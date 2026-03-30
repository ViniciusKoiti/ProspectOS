package dev.prospectos.infrastructure.mcp.service;

import dev.prospectos.api.mcp.PerformanceDataPoint;
import dev.prospectos.api.mcp.ProviderPerformanceData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

final class QueryHistoryPerformanceGenerator {

    private final Random random;

    QueryHistoryPerformanceGenerator(Random random) {
        this.random = random;
    }

    ProviderPerformanceData generate(String provider, String metric) {
        var dataPoints = IntStream.range(0, 24).mapToObj(hour -> new PerformanceDataPoint(
            LocalDateTime.now().minusHours(hour).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            metricValue(metric),
            Map.of("provider", provider, "sampleSize", 10 + random.nextInt(40))
        )).toList();
        var values = dataPoints.stream().mapToDouble(PerformanceDataPoint::value).toArray();
        var summary = new HashMap<String, Object>();
        summary.put("average", Arrays.stream(values).average().orElse(0.0));
        summary.put("minimum", Arrays.stream(values).min().orElse(0.0));
        summary.put("maximum", Arrays.stream(values).max().orElse(0.0));
        summary.put("trend", List.of("IMPROVING", "STABLE", "DEGRADING").get(random.nextInt(3)));
        summary.put("dataPoints", dataPoints.size());
        return new ProviderPerformanceData(provider, metric, dataPoints, summary);
    }

    private double metricValue(String metric) {
        return switch (metric.toLowerCase()) {
            case "response_time" -> 500 + random.nextDouble() * 1000;
            case "success_rate" -> 0.8 + random.nextDouble() * 0.2;
            case "cost_per_query" -> 0.001 + random.nextDouble() * 0.009;
            case "throughput" -> 10 + random.nextDouble() * 40;
            default -> random.nextDouble() * 100;
        };
    }
}
