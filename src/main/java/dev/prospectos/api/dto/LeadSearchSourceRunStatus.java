package dev.prospectos.api.dto;

/**
 * Per-source execution status in async lead search jobs.
 */
public enum LeadSearchSourceRunStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    TIMEOUT
}
