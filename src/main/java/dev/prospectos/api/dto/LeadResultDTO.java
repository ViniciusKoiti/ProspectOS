package dev.prospectos.api.dto;

/**
 * Result DTO for a single lead.
 */
public record LeadResultDTO(
    CompanyDTO company,
    ScoreDTO score,
    SourceProvenanceDTO source
) {
}
