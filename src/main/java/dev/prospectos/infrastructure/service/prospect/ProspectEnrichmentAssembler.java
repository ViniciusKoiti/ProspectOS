package dev.prospectos.infrastructure.service.prospect;

import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.Website;
import dev.prospectos.core.enrichment.EnrichmentRequest;
import dev.prospectos.core.enrichment.EnrichmentResult;
import org.springframework.stereotype.Component;

@Component
public final class ProspectEnrichmentAssembler {

    EnrichmentRequest mergeWithRequest(EnrichmentRequest enrichmentRequest, String fallbackName, String fallbackIndustry) {
        return new EnrichmentRequest(
            firstNonBlank(fallbackName, enrichmentRequest.companyName()),
            enrichmentRequest.description(),
            enrichmentRequest.emails(),
            enrichmentRequest.phone(),
            enrichmentRequest.technologies(),
            firstNonBlank(fallbackIndustry, enrichmentRequest.industry()),
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
            name = firstNonBlank(name, enrichmentResult.normalizedCompanyName());
            industry = firstNonBlank(industry, enrichmentResult.standardizedIndustry());
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
