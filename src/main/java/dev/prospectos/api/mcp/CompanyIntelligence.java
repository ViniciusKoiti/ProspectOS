package dev.prospectos.api.mcp;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Company intelligence data.
 */
public record CompanyIntelligence(
    int estimatedEmployees,
    String revenue,
    String fundingStage,
    List<String> keyPersons,
    Map<String, Object> socialMetrics
) {

    public CompanyIntelligence {
        Objects.requireNonNull(revenue, "revenue must not be null");
        Objects.requireNonNull(fundingStage, "fundingStage must not be null");
        keyPersons = List.copyOf(Objects.requireNonNull(keyPersons, "keyPersons must not be null"));
        socialMetrics = Map.copyOf(Objects.requireNonNull(socialMetrics, "socialMetrics must not be null"));
    }
}
