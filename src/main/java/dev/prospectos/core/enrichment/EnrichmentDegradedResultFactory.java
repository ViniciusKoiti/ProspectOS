package dev.prospectos.core.enrichment;

import java.util.List;

import dev.prospectos.core.domain.Website;

final class EnrichmentDegradedResultFactory {

    private static final int TOTAL_FIELDS = 6;
    private final EnrichmentWebsiteResolver websiteResolver;

    EnrichmentDegradedResultFactory(EnrichmentWebsiteResolver websiteResolver) {
        this.websiteResolver = websiteResolver;
    }

    EnrichmentResult create(EnrichmentRequest request) {
        String fallbackName = request.companyName() != null ? request.companyName().trim() : "Unknown Company";
        Website fallbackWebsite = websiteResolver.parseFallback(request.website());
        int totalEmails = request.emails() != null ? request.emails().size() : 0;
        int fieldsEnriched = fallbackWebsite != null ? 1 : 0;

        EnrichmentQuality degradedQuality = EnrichmentQuality.calculate(
            totalEmails,
            0,
            0,
            0,
            0,
            totalEmails,
            fieldsEnriched,
            TOTAL_FIELDS
        );

        return new EnrichmentResult(
            fallbackName,
            request.description(),
            List.of(),
            request.phone(),
            request.technologies(),
            request.industry() != null ? request.industry() : "Other",
            null,
            request.recentNews(),
            fallbackWebsite,
            degradedQuality
        );
    }
}
