package dev.prospectos.api.mcp;

import java.util.Map;
import java.util.Objects;

/**
 * Basic lead data structure.
 */
public record LeadData(
    String id,
    String companyName,
    String website,
    String industry,
    String country,
    String city,
    Map<String, Object> additionalData,
    double qualityScore
) {

    public LeadData {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(companyName, "companyName must not be null");
        Objects.requireNonNull(website, "website must not be null");
        Objects.requireNonNull(industry, "industry must not be null");
        Objects.requireNonNull(country, "country must not be null");
        Objects.requireNonNull(city, "city must not be null");
        additionalData = Map.copyOf(Objects.requireNonNull(additionalData, "additionalData must not be null"));
    }
}
