package dev.prospectos.infrastructure.mcp.service;

import dev.prospectos.api.mcp.SearchStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

final class InternationalStrategyOptimizer {

    private static final List<String> SOURCES = List.of("nominatim", "bing-maps", "google-places", "web-scraping", "linkedin-api", "crunchbase");
    private final Random random;

    InternationalStrategyOptimizer(Random random) {
        this.random = random;
    }

    SearchStrategy optimize(String market, double budget, double qualityThreshold) {
        var recommendedSources = selectOptimalSources(budget);
        return new SearchStrategy(recommendedSources, sourceWeights(recommendedSources), budget * 0.8, qualityThreshold + 0.05, optimizationTips(market, budget));
    }

    private List<String> selectOptimalSources(double budget) {
        List<String> sources = new ArrayList<>(SOURCES);
        if (budget < 100) {
            sources = new ArrayList<>(sources.subList(0, 3));
        } else {
            sources.add("premium-database");
            sources.add("ai-enrichment");
        }
        Collections.shuffle(sources, random);
        return sources.subList(0, 3 + random.nextInt(2));
    }

    private Map<String, Double> sourceWeights(List<String> sources) {
        var weights = new HashMap<String, Double>();
        var remainingWeight = 1.0;
        for (int index = 0; index < sources.size(); index++) {
            var weight = index == sources.size() - 1 ? remainingWeight : remainingWeight * (0.2 + random.nextDouble() * 0.3);
            weights.put(sources.get(index), weight);
            remainingWeight -= weight;
        }
        return weights;
    }

    private List<String> optimizationTips(String market, double budget) {
        var tips = new ArrayList<String>();
        if (budget < 100) {
            tips.add("Consider increasing budget for better data quality");
            tips.add("Focus on specific industry segments to maximize ROI");
        } else {
            tips.add("Leverage premium data sources for competitive advantage");
            tips.add("Implement multi-source validation for accuracy");
        }
        tips.add("Use local language processing for better results in " + market);
        tips.add("Schedule searches during peak business hours in target timezone");
        return tips;
    }
}
