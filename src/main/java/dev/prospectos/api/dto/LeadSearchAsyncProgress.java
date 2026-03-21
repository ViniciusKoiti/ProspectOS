package dev.prospectos.api.dto;

/**
 * Progress counters for async lead search jobs.
 */
public record LeadSearchAsyncProgress(
    int doneSources,
    int totalSources,
    int failedSources
) {
}
