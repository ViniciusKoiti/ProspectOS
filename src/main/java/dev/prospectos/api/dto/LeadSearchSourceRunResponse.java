package dev.prospectos.api.dto;

/**
 * Status payload for one source in an async lead search job.
 */
public record LeadSearchSourceRunResponse(
    String sourceName,
    LeadSearchSourceRunStatus status,
    Long durationMs,
    String message
) {
}
