package dev.prospectos.core.enrichment;

import java.util.Map;

final class IndustryStandardizer {

    private static final Map<String, String> INDUSTRY_MAPPINGS = Map.ofEntries(
        Map.entry("tech", "Technology"),
        Map.entry("technology", "Technology"),
        Map.entry("software", "Technology"),
        Map.entry("it", "Technology"),
        Map.entry("finance", "Financial Services"),
        Map.entry("fintech", "Financial Services"),
        Map.entry("healthcare", "Healthcare"),
        Map.entry("health", "Healthcare"),
        Map.entry("retail", "Retail"),
        Map.entry("e-commerce", "Retail"),
        Map.entry("manufacturing", "Manufacturing"),
        Map.entry("education", "Education"),
        Map.entry("consulting", "Consulting")
    );

    private final EnrichmentTextNormalizer textNormalizer;

    IndustryStandardizer(EnrichmentTextNormalizer textNormalizer) {
        this.textNormalizer = textNormalizer;
    }

    String standardizeIndustry(String industry) {
        if (industry == null || industry.trim().isEmpty()) {
            return "Other";
        }
        String normalized = industry.trim().toLowerCase();
        String mapped = INDUSTRY_MAPPINGS.get(normalized);
        if (mapped != null) {
            return mapped;
        }
        if (normalized.contains("fintech") || normalized.contains("finance") || normalized.contains("financial")) {
            return "Financial Services";
        }
        if (normalized.contains("healthcare") || normalized.contains("health")) {
            return "Healthcare";
        }
        if (normalized.contains("e-commerce") || normalized.contains("retail")) {
            return "Retail";
        }
        if (normalized.contains("manufacturing")) {
            return "Manufacturing";
        }
        if (normalized.contains("education")) {
            return "Education";
        }
        if (normalized.contains("consulting")) {
            return "Consulting";
        }
        if (normalized.contains("software") || normalized.contains("technology") || normalized.contains("tech")
            || normalized.contains("it")) {
            return "Technology";
        }
        return textNormalizer.capitalizeWords(industry.trim());
    }
}
