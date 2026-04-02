package dev.prospectos.api.mcp;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Provider performance metrics over time.
 */
public record ProviderPerformanceData(
    String provider,
    String metric,
    List<PerformanceDataPoint> dataPoints,
    Map<String, Object> summary
) {

    public ProviderPerformanceData {
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(metric, "metric must not be null");
        dataPoints = List.copyOf(Objects.requireNonNull(dataPoints, "dataPoints must not be null"));
        summary = Map.copyOf(Objects.requireNonNull(summary, "summary must not be null"));
    }
}
