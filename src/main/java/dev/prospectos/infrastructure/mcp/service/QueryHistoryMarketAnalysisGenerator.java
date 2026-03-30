package dev.prospectos.infrastructure.mcp.service;

import dev.prospectos.api.mcp.CompetitorData;
import dev.prospectos.api.mcp.MarketAnalysisData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

final class QueryHistoryMarketAnalysisGenerator {

    private final Random random;

    QueryHistoryMarketAnalysisGenerator(Random random) {
        this.random = random;
    }

    MarketAnalysisData generate(String country, String industry) {
        var metrics = new HashMap<String, Object>();
        metrics.put("marketSize", 1000000 + random.nextInt(9000000));
        metrics.put("growthRate", 0.05 + random.nextDouble() * 0.15);
        metrics.put("competitionLevel", random.nextDouble());
        metrics.put("averageCompanySize", 10 + random.nextInt(990));
        metrics.put("digitalMaturity", random.nextDouble());
        metrics.put("regulatoryComplexity", random.nextDouble());
        return new MarketAnalysisData(country, industry, metrics, insights(country, industry), competitors(country));
    }

    private List<String> insights(String country, String industry) {
        return List.of(
            "Market shows strong growth potential in " + country,
            industry + " sector is experiencing digital transformation",
            "Regulatory environment is " + (random.nextBoolean() ? "favorable" : "challenging"),
            "Competition level is " + (random.nextBoolean() ? "high" : "moderate"),
            "Local partnerships recommended for market entry"
        );
    }

    private Map<String, CompetitorData> competitors(String country) {
        var competitors = new HashMap<String, CompetitorData>();
        for (int index = 1; index <= 3; index++) {
            var name = "Competitor " + index + " (" + country + ")";
            competitors.put(name, new CompetitorData(name, 0.1 + random.nextDouble() * 0.3, List.of("Strong brand presence", "Established customer base", "Local expertise"), List.of("Limited digital presence", "Higher pricing", "Slow innovation")));
        }
        return competitors;
    }
}
