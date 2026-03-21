package dev.prospectos.infrastructure.service.leads;

import dev.prospectos.api.LeadSearchService;
import dev.prospectos.api.dto.LeadResultDTO;
import dev.prospectos.api.dto.LeadSearchRequest;
import dev.prospectos.api.dto.LeadSearchResponse;
import dev.prospectos.api.dto.LeadSearchSourceRunStatus;
import dev.prospectos.api.dto.LeadSearchStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Executes source fan-out and aggregates results for async lead search jobs.
 */
@Component
class LeadSearchAsyncJobProcessor {

    private final LeadSearchService leadSearchService;
    private final LeadSearchAsyncProperties properties;
    private final LeadSearchAsyncSseHub sseHub;
    private final ScraperLeadResultRanker resultRanker = new ScraperLeadResultRanker();

    LeadSearchAsyncJobProcessor(
        LeadSearchService leadSearchService,
        LeadSearchAsyncProperties properties,
        LeadSearchAsyncSseHub sseHub
    ) {
        this.leadSearchService = leadSearchService;
        this.properties = properties;
        this.sseHub = sseHub;
    }

    void process(LeadSearchAsyncJob job, LeadSearchRequest request, List<String> sources) {
        int parallelism = Math.max(1, Math.min(properties.maxParallelSources(), sources.size()));
        ExecutorService sourceExecutor = Executors.newFixedThreadPool(parallelism);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (String source : sources) {
                futures.add(runSource(job, request, source, sourceExecutor));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            finalizeJob(job);
        } finally {
            sourceExecutor.shutdownNow();
            sseHub.complete(job.requestId());
        }
    }

    private CompletableFuture<Void> runSource(
        LeadSearchAsyncJob job,
        LeadSearchRequest request,
        String source,
        ExecutorService sourceExecutor
    ) {
        job.markSourceRunning(source);
        sseHub.publish(job.snapshot());
        return CompletableFuture.runAsync(() -> executeSource(job, request, source), sourceExecutor)
            .orTimeout(properties.sourceTimeoutSeconds(), TimeUnit.SECONDS)
            .exceptionally(error -> {
                if (error instanceof TimeoutException) {
                    job.markSourceFailed(source, "Source timed out", LeadSearchSourceRunStatus.TIMEOUT);
                } else {
                    String message = error.getMessage() == null ? "Source execution failed" : error.getMessage();
                    job.markSourceFailed(source, message, LeadSearchSourceRunStatus.FAILED);
                }
                sseHub.publish(job.snapshot());
                return null;
            });
    }

    private void executeSource(LeadSearchAsyncJob job, LeadSearchRequest request, String source) {
        LeadSearchRequest sourceRequest = new LeadSearchRequest(request.query(), job.limit(), List.of(source), request.icpId());
        LeadSearchResponse response = leadSearchService.searchLeads(sourceRequest);
        if (response.status() == LeadSearchStatus.FAILED) {
            String message = response.message() == null ? "Source failed" : response.message();
            job.markSourceFailed(source, message, LeadSearchSourceRunStatus.FAILED);
        } else {
            job.markSourceCompleted(source, response.leads(), response.message());
        }
        sseHub.publish(job.snapshot());
    }

    private void finalizeJob(LeadSearchAsyncJob job) {
        List<LeadResultDTO> finalLeads = resultRanker.deduplicateSortAndLimit(job.aggregatedLeads(), job.limit());
        if (finalLeads.isEmpty() && job.snapshot().progress().failedSources() > 0) {
            job.markFailed("All sources failed");
        } else if (job.snapshot().progress().failedSources() > 0) {
            job.markCompleted(finalLeads, "Search completed with partial failures");
        } else {
            job.markCompleted(finalLeads, "Search completed");
        }
        sseHub.publish(job.snapshot());
    }
}
