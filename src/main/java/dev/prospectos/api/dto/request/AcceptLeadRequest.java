package dev.prospectos.api.dto.request;

import dev.prospectos.api.dto.CompanyCandidateDTO;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.SourceProvenanceDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request to accept a lead from preview and persist it.
 * Contains the full lead data from preview plus leadKey for idempotency.
 */
public record AcceptLeadRequest(
    @NotBlank(message = "leadKey is required")
    String leadKey,
    @NotNull(message = "candidate is required")
    @Valid
    CompanyCandidateDTO candidate,
    @Valid
    ScoreDTO score,
    @NotNull(message = "source is required")
    @Valid
    SourceProvenanceDTO source
) {
}
