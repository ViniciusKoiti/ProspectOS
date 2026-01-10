package dev.prospectos.api.dto;

import java.time.Instant;

/**
 * Provenance data for a lead source.
 */
public record SourceProvenanceDTO(
    String sourceName,
    String sourceUrl,
    Instant collectedAt
) {
}
