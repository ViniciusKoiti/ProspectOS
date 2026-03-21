package dev.prospectos.infrastructure.service.leads;

import dev.prospectos.api.LeadSearchAsyncService;
import dev.prospectos.api.dto.LeadSearchAsyncSnapshotResponse;
import dev.prospectos.api.dto.LeadSearchAsyncStartResponse;
import dev.prospectos.api.dto.LeadSearchRequest;
import dev.prospectos.api.dto.LeadSearchStatus;
import dev.prospectos.infrastructure.service.compliance.AllowedSourcesComplianceService;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Async lead search service backed by in-memory jobs and SSE notifications.
 */
@Service
class EventDrivenLeadSearchAsyncService implements LeadSearchAsyncService, LeadSearchAsyncStreamService {

    private static final int DEFAULT_LIMIT = 10;

    private final AllowedSourcesComplianceService complianceService;
    private final TaskExecutor taskExecutor;
    private final LeadSearchAsyncJobStore jobStore;
    private final LeadSearchAsyncJobProcessor jobProcessor;
    private final LeadSearchAsyncSseHub sseHub;

    EventDrivenLeadSearchAsyncService(
        AllowedSourcesComplianceService complianceService,
        TaskExecutor taskExecutor,
        LeadSearchAsyncJobStore jobStore,
        LeadSearchAsyncJobProcessor jobProcessor,
        LeadSearchAsyncSseHub sseHub
    ) {
        this.complianceService = complianceService;
        this.taskExecutor = taskExecutor;
        this.jobStore = jobStore;
        this.jobProcessor = jobProcessor;
        this.sseHub = sseHub;
    }

    @Override
    public LeadSearchAsyncStartResponse startSearch(LeadSearchRequest request) {
        validateRequest(request);
        List<String> sources = complianceService.validateSources(request.sources());
        if (sources.isEmpty()) {
            throw new IllegalArgumentException("No lead sources configured");
        }
        int limit = request.limit() == null ? DEFAULT_LIMIT : request.limit();
        Instant createdAt = Instant.now();
        LeadSearchAsyncJob job = new LeadSearchAsyncJob(UUID.randomUUID(), limit, sources, createdAt);
        jobStore.put(job);
        sseHub.publish(job.snapshot());
        taskExecutor.execute(() -> jobProcessor.process(job, request, sources));
        return new LeadSearchAsyncStartResponse(job.requestId(), LeadSearchStatus.PROCESSING, "Search started", createdAt);
    }

    @Override
    public Optional<LeadSearchAsyncSnapshotResponse> getSnapshot(UUID requestId) {
        if (requestId == null) {
            return Optional.empty();
        }
        return jobStore.get(requestId).map(LeadSearchAsyncJob::snapshot);
    }

    @Override
    public SseEmitter subscribe(UUID requestId) {
        LeadSearchAsyncSnapshotResponse snapshot = getSnapshot(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Search request not found: " + requestId));
        return sseHub.subscribe(requestId, snapshot);
    }

    private void validateRequest(LeadSearchRequest request) {
        if (request == null || request.query() == null || request.query().isBlank()) {
            throw new IllegalArgumentException("Query cannot be null or blank");
        }
    }
}
