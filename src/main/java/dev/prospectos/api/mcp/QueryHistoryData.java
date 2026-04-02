package dev.prospectos.api.mcp;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Historical query execution data.
 */
public record QueryHistoryData(
    QueryTimeWindow timeWindow,
    String provider,
    List<QueryExecution> executions,
    Map<String, Object> aggregatedMetrics
) {

    public QueryHistoryData {
        Objects.requireNonNull(timeWindow, "timeWindow must not be null");
        executions = List.copyOf(Objects.requireNonNull(executions, "executions must not be null"));
        aggregatedMetrics = Map.copyOf(Objects.requireNonNull(aggregatedMetrics, "aggregatedMetrics must not be null"));
    }
}
