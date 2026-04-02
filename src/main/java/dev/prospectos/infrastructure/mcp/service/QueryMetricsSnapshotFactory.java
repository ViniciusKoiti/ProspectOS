package dev.prospectos.infrastructure.mcp.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dev.prospectos.api.mcp.QueryMetricsSnapshot;
import dev.prospectos.api.mcp.QueryTimeWindow;

final class QueryMetricsSnapshotFactory {

    private final QueryMetricsTrendCalculator trendCalculator = new QueryMetricsTrendCalculator();

    QueryMetricsSnapshot create(List<QueryMetricsObservation> observations, QueryTimeWindow timeWindow, Instant now) {
        Instant cutoff = now.minus(timeWindow.duration());
        Instant previousCutoff = cutoff.minus(timeWindow.duration());
        List<QueryMetricsObservation> currentWindow = filterCurrentWindow(observations, cutoff, now);
        List<QueryMetricsObservation> previousWindow = filterBetween(observations, previousCutoff, cutoff);
        return new QueryMetricsSnapshot(
            currentWindow.size(),
            totalCost(currentWindow),
            averageCost(currentWindow),
            successRate(currentWindow),
            averageDuration(currentWindow),
            trendCalculator.calculate(currentWindow, previousWindow),
            breakdown(currentWindow)
        );
    }

    private List<QueryMetricsObservation> filterCurrentWindow(List<QueryMetricsObservation> observations, Instant from, Instant now) {
        return observations.stream()
            .filter(observation -> !observation.recordedAt().isBefore(from))
            .filter(observation -> !observation.recordedAt().isAfter(now))
            .toList();
    }

    private List<QueryMetricsObservation> filterBetween(List<QueryMetricsObservation> observations, Instant from, Instant to) {
        return observations.stream()
            .filter(observation -> !observation.recordedAt().isBefore(from))
            .filter(observation -> observation.recordedAt().isBefore(to))
            .toList();
    }

    private List<QueryMetricsSnapshot.ProviderMetric> breakdown(List<QueryMetricsObservation> observations) {
        Map<String, List<QueryMetricsObservation>> grouped = observations.stream()
            .collect(Collectors.groupingBy(QueryMetricsObservation::provider));
        return grouped.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> new QueryMetricsSnapshot.ProviderMetric(
                entry.getKey(),
                entry.getValue().size(),
                totalCost(entry.getValue()),
                successRate(entry.getValue()),
                averageDuration(entry.getValue())
            ))
            .toList();
    }

    private BigDecimal totalCost(List<QueryMetricsObservation> observations) {
        return observations.stream()
            .map(QueryMetricsObservation::estimatedCost)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal averageCost(List<QueryMetricsObservation> observations) {
        if (observations.isEmpty()) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        return totalCost(observations).divide(BigDecimal.valueOf(observations.size()), 4, RoundingMode.HALF_UP);
    }

    private double successRate(List<QueryMetricsObservation> observations) {
        if (observations.isEmpty()) {
            return 0.0d;
        }
        long successful = observations.stream().filter(QueryMetricsObservation::success).count();
        return BigDecimal.valueOf((double) successful / observations.size()).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }

    private long averageDuration(List<QueryMetricsObservation> observations) {
        if (observations.isEmpty()) {
            return 0L;
        }
        return Math.round(observations.stream().mapToLong(QueryMetricsObservation::durationMs).average().orElse(0.0d));
    }
}
