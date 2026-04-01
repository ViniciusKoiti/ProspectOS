package dev.prospectos.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record LeadRecommendationResponse(
    String recommendedSource,
    List<String> fallbackSources,
    String reason,
    BigDecimal expectedCost,
    long expectedLatencyMs,
    String timeWindow
) {
    public LeadRecommendationResponse {
        Objects.requireNonNull(recommendedSource, "recommendedSource must not be null");
        fallbackSources = List.copyOf(fallbackSources == null ? List.of() : fallbackSources);
        Objects.requireNonNull(reason, "reason must not be null");
        Objects.requireNonNull(expectedCost, "expectedCost must not be null");
        Objects.requireNonNull(timeWindow, "timeWindow must not be null");
    }
}
