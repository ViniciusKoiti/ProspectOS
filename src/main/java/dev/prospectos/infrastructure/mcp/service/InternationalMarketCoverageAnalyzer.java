package dev.prospectos.infrastructure.mcp.service;

import dev.prospectos.api.mcp.CompetitorAnalysis;
import dev.prospectos.api.mcp.MarketCoverageAnalysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

final class InternationalMarketCoverageAnalyzer {

    private final Random random;

    InternationalMarketCoverageAnalyzer(Random random) {
        this.random = random;
    }

    MarketCoverageAnalysis analyze(String country, List<String> competitors) {
        var uncoveredSegments = List.of("Small businesses (1-10 employees)", "Rural enterprises", "Emerging technology startups", "Traditional family businesses");
        return new MarketCoverageAnalysis(
            country,
            marketSize(country),
            0.15 + (random.nextDouble() * 0.25),
            uncoveredSegments,
            competitorAnalysis(competitors),
            List.of(
                "Target " + uncoveredSegments.get(0) + " with specialized offering",
                "Develop local partnerships for market entry",
                "Leverage digital channels for competitive advantage",
                "Focus on cost-effective solutions for price-sensitive segments"
            )
        );
    }

    private double marketSize(String country) {
        var baseSize = Map.of("brazil", 50000000.0, "argentina", 20000000.0, "chile", 8000000.0, "mexico", 45000000.0, "spain", 25000000.0, "italy", 30000000.0).getOrDefault(country.toLowerCase(), 10000000.0);
        return baseSize * (0.8 + random.nextDouble() * 0.4);
    }

    private Map<String, CompetitorAnalysis> competitorAnalysis(List<String> competitors) {
        return competitors.stream().collect(HashMap::new, (map, competitor) -> map.put(competitor, new CompetitorAnalysis(competitor, 0.05 + random.nextDouble() * 0.2, List.of("Enterprise", "SMB", "Startups").subList(0, 1 + random.nextInt(2)), List.of("Strong brand", "Established network", "Local expertise"), List.of("Limited digital presence", "Higher pricing", "Slow innovation"))), HashMap::putAll);
    }
}
