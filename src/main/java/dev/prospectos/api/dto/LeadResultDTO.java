package dev.prospectos.api.dto;

/**
 * Result DTO for a single lead from search preview.
 * Contains candidate data (not yet persisted) with leadKey for idempotent accept.
 */
public record LeadResultDTO(
    CompanyCandidateDTO candidate,
    ScoreDTO score,
    SourceProvenanceDTO source,
    String leadKey
) {
}
