package dev.prospectos.api.dto;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for on-demand lead search.
 */
public record LeadSearchRequest(
    String query,
    Integer limit,
    List<String> sources,
    UUID icpId
) {
}
