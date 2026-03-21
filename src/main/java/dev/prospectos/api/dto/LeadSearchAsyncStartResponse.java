package dev.prospectos.api.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Accepted response for starting an async lead search job.
 */
public record LeadSearchAsyncStartResponse(
    UUID requestId,
    LeadSearchStatus status,
    String message,
    Instant createdAt
) {
}
