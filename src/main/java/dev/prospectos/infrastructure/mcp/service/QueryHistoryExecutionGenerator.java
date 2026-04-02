package dev.prospectos.infrastructure.mcp.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dev.prospectos.api.mcp.QueryExecution;
import dev.prospectos.api.mcp.QueryHistoryData;
import dev.prospectos.api.mcp.QueryTimeWindow;

final class QueryHistoryExecutionGenerator {

    QueryHistoryData generate(QueryTimeWindow timeWindow, String provider, List<QueryMetricsObservation> observations) {
        List<QueryExecution> executions = observations.stream()
            .sorted((left, right) -> right.recordedAt().compareTo(left.recordedAt()))
            .map(this::execution)
            .toList();
        return new QueryHistoryData(timeWindow, provider, executions, aggregatedMetrics(observations));
    }

    private QueryExecution execution(QueryMetricsObservation observation) {
        return new QueryExecution(
            observation.recordedAt().toString(),
            observation.operation(),
            observation.provider(),
            observation.success(),
            observation.durationMs(),
            observation.estimatedCost().doubleValue(),
            observation.success() ? null : "Execution failed"
        );
    }

    private Map<String, Object> aggregatedMetrics(List<QueryMetricsObservation> observations) {
        int totalQueries = observations.size();
        long successfulQueries = observations.stream().filter(QueryMetricsObservation::success).count();
        BigDecimal totalCost = observations.stream().map(QueryMetricsObservation::estimatedCost).reduce(BigDecimal.ZERO, BigDecimal::add);
        double averageResponseTime = observations.stream().mapToLong(QueryMetricsObservation::durationMs).average().orElse(0.0d);
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("totalQueries", totalQueries);
        metrics.put("successfulQueries", successfulQueries);
        metrics.put("successRate", totalQueries == 0 ? 0.0d : successfulQueries / (double) totalQueries);
        metrics.put("totalCost", totalCost.setScale(2, RoundingMode.HALF_UP).doubleValue());
        metrics.put("averageResponseTime", averageResponseTime);
        metrics.put("costPerQuery", totalQueries == 0 ? 0.0d : totalCost.divide(BigDecimal.valueOf(totalQueries), 4, RoundingMode.HALF_UP).doubleValue());
        return metrics;
    }
}
