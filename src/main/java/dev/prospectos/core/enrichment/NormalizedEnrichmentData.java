package dev.prospectos.core.enrichment;

import dev.prospectos.core.domain.CompanySize;

record NormalizedEnrichmentData(
    String normalizedName,
    String cleanDescription,
    String normalizedPhone,
    String standardizedIndustry,
    CompanySize size
) {
}

