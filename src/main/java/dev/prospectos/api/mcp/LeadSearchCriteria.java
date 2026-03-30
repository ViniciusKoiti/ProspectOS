package dev.prospectos.api.mcp;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Lead search criteria for international discovery.
 */
public record LeadSearchCriteria(
    int maxResults,
    double budgetLimit,
    double minQualityScore,
    List<String> requiredFields,
    Map<String, String> filters
) {

    public LeadSearchCriteria {
        requiredFields = List.copyOf(Objects.requireNonNull(requiredFields, "requiredFields must not be null"));
        filters = Map.copyOf(Objects.requireNonNull(filters, "filters must not be null"));
    }
}
