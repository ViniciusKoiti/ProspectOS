package dev.prospectos.api.mcp;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Market analysis intelligence data.
 */
public record MarketAnalysisData(
    String country,
    String industry,
    Map<String, Object> marketMetrics,
    List<String> insights,
    Map<String, CompetitorData> competitors
) {

    public MarketAnalysisData {
        Objects.requireNonNull(country, "country must not be null");
        Objects.requireNonNull(industry, "industry must not be null");
        marketMetrics = Map.copyOf(Objects.requireNonNull(marketMetrics, "marketMetrics must not be null"));
        insights = List.copyOf(Objects.requireNonNull(insights, "insights must not be null"));
        competitors = Map.copyOf(Objects.requireNonNull(competitors, "competitors must not be null"));
    }
}
