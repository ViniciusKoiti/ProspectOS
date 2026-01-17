package dev.prospectos.api.dto.request;

import dev.prospectos.api.dto.CompanyCandidateDTO;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.SourceProvenanceDTO;

/**
 * Request to accept a lead from preview and persist it.
 * Contains the full lead data from preview plus leadKey for idempotency.
 */
public record AcceptLeadRequest(
    String leadKey,
    CompanyCandidateDTO candidate,
    ScoreDTO score,
    SourceProvenanceDTO source
) {
    public AcceptLeadRequest {
        if (leadKey == null || leadKey.isBlank()) {
            throw new IllegalArgumentException("leadKey is required");
        }
        if (candidate == null) {
            throw new IllegalArgumentException("candidate is required");
        }
        if (source == null) {
            throw new IllegalArgumentException("source is required");
        }
    }
}
