package dev.prospectos.infrastructure.service.discovery;

import dev.prospectos.api.dto.CompanyDTO;

/**
 * Result of semantic company search.
 */
public record SemanticCompanyMatch(
    CompanyDTO company,
    double similarity
) {
}
