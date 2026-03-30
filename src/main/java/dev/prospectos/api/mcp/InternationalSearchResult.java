package dev.prospectos.api.mcp;

import java.util.List;
import java.util.Objects;

/**
 * International search results with quality metrics.
 */
public record InternationalSearchResult(
    String country,
    String industry,
    List<LeadData> leads,
    SearchQualityMetrics quality,
    double totalCost,
    List<String> sourcesUsed
) {

    public InternationalSearchResult {
        Objects.requireNonNull(country, "country must not be null");
        Objects.requireNonNull(industry, "industry must not be null");
        leads = List.copyOf(Objects.requireNonNull(leads, "leads must not be null"));
        Objects.requireNonNull(quality, "quality must not be null");
        sourcesUsed = List.copyOf(Objects.requireNonNull(sourcesUsed, "sourcesUsed must not be null"));
    }
}
