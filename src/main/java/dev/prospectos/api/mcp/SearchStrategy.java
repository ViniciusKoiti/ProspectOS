package dev.prospectos.api.mcp;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Optimized search strategy.
 */
public record SearchStrategy(
    List<String> recommendedSources,
    Map<String, Double> sourceWeights,
    double estimatedCost,
    double estimatedQuality,
    List<String> optimizationTips
) {

    public SearchStrategy {
        recommendedSources = List.copyOf(Objects.requireNonNull(recommendedSources, "recommendedSources must not be null"));
        sourceWeights = Map.copyOf(Objects.requireNonNull(sourceWeights, "sourceWeights must not be null"));
        optimizationTips = List.copyOf(Objects.requireNonNull(optimizationTips, "optimizationTips must not be null"));
    }
}
