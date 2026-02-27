package dev.prospectos.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

/**
 * Provenance data for a lead source.
 */
public record SourceProvenanceDTO(
    @NotBlank(message = "source.sourceName is required")
    String sourceName,
    @NotBlank(message = "source.sourceUrl is required")
    String sourceUrl,
    @NotNull(message = "source.collectedAt is required")
    Instant collectedAt
) {
}
