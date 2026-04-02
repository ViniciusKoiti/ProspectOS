package dev.prospectos.infrastructure.mcp.dto;

import dev.prospectos.api.mcp.*;

import java.util.List;
import java.util.Map;

public record QueryHistoryResponse(
    String timeWindow,
    String provider,
    List<QueryExecutionResponse> executions,
    Map<String, Object> aggregatedMetrics
) {

    public static QueryHistoryResponse fromDomain(QueryHistoryData data) {
        var executions = data.executions().stream()
            .map(QueryExecutionResponse::fromDomain)
            .toList();

        return new QueryHistoryResponse(
            data.timeWindow().value(),
            data.provider(),
            executions,
            data.aggregatedMetrics()
        );
    }

    public record QueryExecutionResponse(
        String timestamp,
        String query,
        String provider,
        boolean success,
        long responseTimeMs,
        double cost,
        String errorMessage
    ) {
        public static QueryExecutionResponse fromDomain(QueryExecution execution) {
            return new QueryExecutionResponse(
                execution.timestamp(),
                execution.query(),
                execution.provider(),
                execution.success(),
                execution.responseTimeMs(),
                execution.cost(),
                execution.errorMessage()
            );
        }
    }
}
