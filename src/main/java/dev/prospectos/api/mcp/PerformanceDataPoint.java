package dev.prospectos.api.mcp;

import java.util.Map;
import java.util.Objects;

/**
 * Time-series performance data point.
 */
public record PerformanceDataPoint(
    String timestamp,
    double value,
    Map<String, Object> metadata
) {

    public PerformanceDataPoint {
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        metadata = Map.copyOf(Objects.requireNonNull(metadata, "metadata must not be null"));
    }
}
