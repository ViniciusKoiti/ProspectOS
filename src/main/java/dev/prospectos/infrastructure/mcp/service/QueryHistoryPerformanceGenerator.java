package dev.prospectos.infrastructure.mcp.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import dev.prospectos.api.mcp.PerformanceDataPoint;
import dev.prospectos.api.mcp.ProviderPerformanceData;

final class QueryHistoryPerformanceGenerator {

    ProviderPerformanceData generate(String provider, String metric, List<QueryMetricsObservation> observations) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.HOURS);
        List<PerformanceDataPoint> dataPoints = IntStream.rangeClosed(0, 23)
            .mapToObj(index -> point(provider, metric, observations, now.minus(index, ChronoUnit.HOURS)))
            .toList();
        return new ProviderPerformanceData(provider, metric, dataPoints, summary(dataPoints));
    }

    private PerformanceDataPoint point(String provider, String metric, List<QueryMetricsObservation> observations, Instant bucketStart) {
        Instant bucketEnd = bucketStart.plus(1, ChronoUnit.HOURS);
        List<QueryMetricsObservation> bucket = observations.stream()
            .filter(observation -> !observation.recordedAt().isBefore(bucketStart) && observation.recordedAt().isBefore(bucketEnd))
            .toList();
        return new PerformanceDataPoint(bucketStart.toString(), metricValue(metric, bucket), Map.of("provider", provider, "sampleSize", bucket.size()));
    }

    private Map<String, Object> summary(List<PerformanceDataPoint> dataPoints) {
        double average = dataPoints.stream().mapToDouble(PerformanceDataPoint::value).average().orElse(0.0d);
        double minimum = dataPoints.stream().mapToDouble(PerformanceDataPoint::value).min().orElse(0.0d);
        double maximum = dataPoints.stream().mapToDouble(PerformanceDataPoint::value).max().orElse(0.0d);
        double first = dataPoints.getLast().value();
        double last = dataPoints.getFirst().value();
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("average", average);
        summary.put("minimum", minimum);
        summary.put("maximum", maximum);
        summary.put("trend", last > first ? "IMPROVING" : last < first ? "DEGRADING" : "STABLE");
        summary.put("dataPoints", dataPoints.size());
        return summary;
    }

    private double metricValue(String metric, List<QueryMetricsObservation> observations) {
        if (observations.isEmpty()) {
            return 0.0d;
        }
        return switch (metric.toLowerCase()) {
            case "response_time" -> observations.stream().mapToLong(QueryMetricsObservation::durationMs).average().orElse(0.0d);
            case "success_rate" -> observations.stream().filter(QueryMetricsObservation::success).count() / (double) observations.size();
            case "cost_per_query" -> observations.stream().mapToDouble(observation -> observation.estimatedCost().doubleValue()).average().orElse(0.0d);
            case "throughput" -> observations.size();
            case "error_rate" -> observations.stream().filter(observation -> !observation.success()).count() / (double) observations.size();
            default -> 0.0d;
        };
    }
}
