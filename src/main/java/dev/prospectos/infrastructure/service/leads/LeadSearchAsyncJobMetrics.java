package dev.prospectos.infrastructure.service.leads;

import dev.prospectos.api.dto.LeadSearchSourceRunResponse;
import dev.prospectos.api.dto.LeadSearchSourceRunStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

final class LeadSearchAsyncJobMetrics {

    private LeadSearchAsyncJobMetrics() {
    }

    static long durationMs(Map<String, Instant> sourceStartTimes, String source) {
        Instant startedAt = sourceStartTimes.get(source);
        if (startedAt == null) {
            return 0L;
        }
        return Math.max(0L, Duration.between(startedAt, Instant.now()).toMillis());
    }

    static int doneSources(Map<String, LeadSearchSourceRunResponse> sourceRuns) {
        return (int) sourceRuns.values().stream()
            .filter(run -> run.status() != LeadSearchSourceRunStatus.PENDING && run.status() != LeadSearchSourceRunStatus.RUNNING)
            .count();
    }

    static int failedSources(Map<String, LeadSearchSourceRunResponse> sourceRuns) {
        return (int) sourceRuns.values().stream()
            .filter(run -> run.status() == LeadSearchSourceRunStatus.FAILED || run.status() == LeadSearchSourceRunStatus.TIMEOUT)
            .count();
    }
}
