package dev.prospectos.infrastructure.service.leads;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory job store for async lead search sessions.
 */
@Component
class LeadSearchAsyncJobStore {

    private final Map<UUID, LeadSearchAsyncJob> jobs = new ConcurrentHashMap<>();

    void put(LeadSearchAsyncJob job) {
        jobs.put(job.requestId(), job);
    }

    Optional<LeadSearchAsyncJob> get(UUID requestId) {
        return Optional.ofNullable(jobs.get(requestId));
    }

    void removeExpired(Instant cutoff) {
        jobs.entrySet().removeIf(entry -> entry.getValue().isExpired(cutoff));
    }
}
