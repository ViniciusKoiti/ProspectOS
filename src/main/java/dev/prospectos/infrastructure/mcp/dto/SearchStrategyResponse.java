package dev.prospectos.infrastructure.mcp.dto;

import dev.prospectos.api.mcp.SearchStrategy;
import java.util.List;
import java.util.Map;

public record SearchStrategyResponse(
    List<String> recommendedSources,
    Map<String, Double> sourceWeights,
    double estimatedCost,
    double estimatedQuality,
    List<String> optimizationTips
) {

    public static SearchStrategyResponse fromDomain(SearchStrategy strategy) {
        return new SearchStrategyResponse(
            strategy.recommendedSources(),
            strategy.sourceWeights(),
            strategy.estimatedCost(),
            strategy.estimatedQuality(),
            strategy.optimizationTips()
        );
    }
}
