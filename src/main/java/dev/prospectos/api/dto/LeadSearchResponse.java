package dev.prospectos.api.dto;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for on-demand lead search.
 */
public record LeadSearchResponse(
    LeadSearchStatus status,
    List<LeadResultDTO> leads,
    UUID requestId,
    String message
) {
}
