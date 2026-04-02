package dev.prospectos.api.mcp;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Market coverage analysis.
 */
public record MarketCoverageAnalysis(
    String country,
    double totalMarketSize,
    double coveredMarketShare,
    List<String> uncoveredSegments,
    Map<String, CompetitorAnalysis> competitorAnalysis,
    List<String> opportunities
) {

    public MarketCoverageAnalysis {
        Objects.requireNonNull(country, "country must not be null");
        uncoveredSegments = List.copyOf(Objects.requireNonNull(uncoveredSegments, "uncoveredSegments must not be null"));
        competitorAnalysis = Map.copyOf(Objects.requireNonNull(competitorAnalysis, "competitorAnalysis must not be null"));
        opportunities = List.copyOf(Objects.requireNonNull(opportunities, "opportunities must not be null"));
    }
}
