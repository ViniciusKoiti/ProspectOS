package dev.prospectos.infrastructure.service.prospect;

import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.Website;
import dev.prospectos.core.enrichment.EnrichmentRequest;
import dev.prospectos.core.enrichment.EnrichmentResult;

final class ProspectEnrichmentAssembler {

    EnrichmentRequest mergeWithRequest(EnrichmentRequest enrichmentRequest, String fallbackName, String fallbackIndustry) {
        return new EnrichmentRequest(
            firstNonBlank(enrichmentRequest.companyName(), fallbackName),
            enrichmentRequest.description(),
            enrichmentRequest.emails(),
            enrichmentRequest.phone(),
            enrichmentRequest.technologies(),
            firstNonBlank(enrichmentRequest.industry(), fallbackIndustry),
            enrichmentRequest.size(),
            enrichmentRequest.recentNews(),
            enrichmentRequest.website()
        );
    }

    Company buildCompany(String fallbackName, String websiteUrl, String fallbackIndustry, EnrichmentResult enrichmentResult) {
        String name = fallbackName;
        String industry = fallbackIndustry;
        Website website = Website.of(websiteUrl);
        if (enrichmentResult != null) {
            name = firstNonBlank(enrichmentResult.normalizedCompanyName(), name);
            industry = firstNonBlank(enrichmentResult.standardizedIndustry(), industry);
            if (enrichmentResult.website() != null) {
                website = enrichmentResult.website();
            }
        }
        return Company.create(name, website, industry);
    }

    String normalizeWebsite(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        return "https://" + trimmed;
    }

    private String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary.trim();
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback.trim();
        }
        return null;
    }
}
