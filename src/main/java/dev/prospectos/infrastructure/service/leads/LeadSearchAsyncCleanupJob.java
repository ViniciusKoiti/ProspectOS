package dev.prospectos.infrastructure.service.leads;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Removes expired async search jobs from in-memory storage.
 */
@Component
class LeadSearchAsyncCleanupJob {

    private final LeadSearchAsyncJobStore jobStore;
    private final LeadSearchAsyncProperties properties;

    LeadSearchAsyncCleanupJob(LeadSearchAsyncJobStore jobStore, LeadSearchAsyncProperties properties) {
        this.jobStore = jobStore;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${prospectos.leads.async.cleanup-interval-ms:300000}")
    void cleanupExpiredJobs() {
        Instant cutoff = Instant.now().minus(properties.jobTtlMinutes(), ChronoUnit.MINUTES);
        jobStore.removeExpired(cutoff);
    }
}
