package dev.prospectos.infrastructure.mcp.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.ToDoubleFunction;

import dev.prospectos.api.mcp.QueryMetricsSnapshot;

final class QueryMetricsTrendCalculator {

    QueryMetricsSnapshot.Trends calculate(List<QueryMetricsObservation> current, List<QueryMetricsObservation> previous) {
        return new QueryMetricsSnapshot.Trends(
            trend(current, previous, this::cost),
            trend(current, previous, this::quality),
            trend(current, previous, observation -> 1.0d)
        );
    }

    private String trend(
        List<QueryMetricsObservation> current,
        List<QueryMetricsObservation> previous,
        ToDoubleFunction<QueryMetricsObservation> metric
    ) {
        double currentValue = aggregate(current, metric);
        double previousValue = aggregate(previous, metric);
        if (previousValue == 0.0d) {
            return currentValue == 0.0d ? "0%" : "+100%";
        }
        double percentage = ((currentValue - previousValue) / previousValue) * 100.0d;
        double rounded = BigDecimal.valueOf(percentage).setScale(0, RoundingMode.HALF_UP).doubleValue();
        return rounded >= 0 ? "+%.0f%%".formatted(rounded) : "%.0f%%".formatted(rounded);
    }

    private double aggregate(List<QueryMetricsObservation> observations, ToDoubleFunction<QueryMetricsObservation> metric) {
        return observations.stream().mapToDouble(metric).sum();
    }

    private double cost(QueryMetricsObservation observation) {
        return observation.estimatedCost().doubleValue();
    }

    private double quality(QueryMetricsObservation observation) {
        return observation.success() ? 1.0d : 0.0d;
    }
}