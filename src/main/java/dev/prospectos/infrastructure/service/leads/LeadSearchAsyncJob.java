package dev.prospectos.infrastructure.service.leads;

import dev.prospectos.api.dto.LeadResultDTO;
import dev.prospectos.api.dto.LeadSearchAsyncProgress;
import dev.prospectos.api.dto.LeadSearchAsyncSnapshotResponse;
import dev.prospectos.api.dto.LeadSearchSourceRunResponse;
import dev.prospectos.api.dto.LeadSearchSourceRunStatus;
import dev.prospectos.api.dto.LeadSearchStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class LeadSearchAsyncJob {
    private final UUID requestId;
    private final int limit;
    private final Instant createdAt;
    private final Map<String, LeadSearchSourceRunResponse> sourceRuns;
    private final Map<String, Instant> sourceStartTimes = new LinkedHashMap<>();
    private final List<LeadResultDTO> aggregatedLeads = new ArrayList<>();
    private LeadSearchStatus status = LeadSearchStatus.PROCESSING;
    private String message = "Lead search started";
    private Instant updatedAt;
    private Instant completedAt;
    LeadSearchAsyncJob(UUID requestId, int limit, List<String> sources, Instant createdAt) {
        this.requestId = requestId;
        this.limit = limit;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.sourceRuns = new LinkedHashMap<>();
        for (String source : sources) {
            sourceRuns.put(source, new LeadSearchSourceRunResponse(source, LeadSearchSourceRunStatus.PENDING, null, null));
        }
    }
    synchronized UUID requestId() {
        return requestId;
    }
    synchronized int limit() {
        return limit;
    }
    synchronized void markSourceRunning(String source) {
        sourceStartTimes.put(source, Instant.now());
        sourceRuns.put(source, new LeadSearchSourceRunResponse(source, LeadSearchSourceRunStatus.RUNNING, null, null));
        updatedAt = Instant.now();
    }
    synchronized void markSourceCompleted(String source, List<LeadResultDTO> leads, String sourceMessage) {
        sourceRuns.put(source, new LeadSearchSourceRunResponse(source, LeadSearchSourceRunStatus.COMPLETED, LeadSearchAsyncJobMetrics.durationMs(sourceStartTimes, source), sourceMessage));
        if (leads != null && !leads.isEmpty()) {
            aggregatedLeads.addAll(leads);
        }
        updatedAt = Instant.now();
    }
    synchronized void markSourceFailed(String source, String sourceMessage, LeadSearchSourceRunStatus failureStatus) {
        sourceRuns.put(source, new LeadSearchSourceRunResponse(source, failureStatus, LeadSearchAsyncJobMetrics.durationMs(sourceStartTimes, source), sourceMessage));
        updatedAt = Instant.now();
    }
    synchronized void markCompleted(List<LeadResultDTO> leads, String finalMessage) {
        status = LeadSearchStatus.COMPLETED;
        message = finalMessage;
        aggregatedLeads.clear();
        aggregatedLeads.addAll(leads);
        completedAt = Instant.now();
        updatedAt = completedAt;
    }
    synchronized void markFailed(String finalMessage) {
        status = LeadSearchStatus.FAILED;
        message = finalMessage;
        completedAt = Instant.now();
        updatedAt = completedAt;
    }
    synchronized List<LeadResultDTO> aggregatedLeads() {
        return List.copyOf(aggregatedLeads);
    }
    synchronized LeadSearchAsyncSnapshotResponse snapshot() {
        return new LeadSearchAsyncSnapshotResponse(
            requestId,
            status,
            message,
            new LeadSearchAsyncProgress(
                LeadSearchAsyncJobMetrics.doneSources(sourceRuns),
                sourceRuns.size(),
                LeadSearchAsyncJobMetrics.failedSources(sourceRuns)
            ),
            List.copyOf(sourceRuns.values()),
            List.copyOf(aggregatedLeads),
            createdAt,
            updatedAt,
            completedAt
        );
    }
    synchronized boolean isExpired(Instant cutoff) {
        Instant reference = completedAt == null ? updatedAt : completedAt;
        return reference.isBefore(cutoff);
    }
}
