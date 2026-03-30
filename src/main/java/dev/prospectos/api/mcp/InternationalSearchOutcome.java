package dev.prospectos.api.mcp;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record InternationalSearchOutcome(
    List<LeadCandidate> leads,
    BigDecimal totalCost,
    double avgQualityScore,
    List<String> optimizationHints
) {

    public InternationalSearchOutcome {
        leads = List.copyOf(leads);
        Objects.requireNonNull(totalCost, "totalCost must not be null");
        optimizationHints = List.copyOf(optimizationHints);
    }

    public record LeadCandidate(
        String name,
        String country,
        String sourceProvider,
        double qualityScore
    ) {
        public LeadCandidate {
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(country, "country must not be null");
            Objects.requireNonNull(sourceProvider, "sourceProvider must not be null");
        }
    }
}
