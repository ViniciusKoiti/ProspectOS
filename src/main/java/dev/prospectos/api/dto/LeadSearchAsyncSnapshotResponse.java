package dev.prospectos.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Snapshot payload for async lead search status and result.
 */
public record LeadSearchAsyncSnapshotResponse(
    UUID requestId,
    LeadSearchStatus status,
    String message,
    LeadSearchAsyncProgress progress,
    List<LeadSearchSourceRunResponse> sourceRuns,
    List<LeadResultDTO> leads,
    Instant createdAt,
    Instant updatedAt,
    Instant completedAt
) {
}
