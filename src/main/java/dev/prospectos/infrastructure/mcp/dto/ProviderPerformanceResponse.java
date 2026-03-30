package dev.prospectos.infrastructure.mcp.dto;

import dev.prospectos.api.mcp.*;

import java.util.List;
import java.util.Map;

public record ProviderPerformanceResponse(
    String provider,
    String metric,
    List<PerformanceDataPointResponse> dataPoints,
    Map<String, Object> summary
) {

    public static ProviderPerformanceResponse fromDomain(ProviderPerformanceData data) {
        var dataPoints = data.dataPoints().stream()
            .map(PerformanceDataPointResponse::fromDomain)
            .toList();

        return new ProviderPerformanceResponse(
            data.provider(),
            data.metric(),
            dataPoints,
            data.summary()
        );
    }

    public record PerformanceDataPointResponse(
        String timestamp,
        double value,
        Map<String, Object> metadata
    ) {
        public static PerformanceDataPointResponse fromDomain(PerformanceDataPoint dataPoint) {
            return new PerformanceDataPointResponse(
                dataPoint.timestamp(),
                dataPoint.value(),
                dataPoint.metadata()
            );
        }
    }
}
