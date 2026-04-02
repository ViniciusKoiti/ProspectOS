package dev.prospectos.infrastructure.mcp.dto;

import dev.prospectos.api.mcp.QueryMetricsSnapshot;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public record QueryMetricsResponse(
    long totalQueries,
    BigDecimal totalCost,
    BigDecimal avgCostPerQuery,
    double successRate,
    long avgResponseTime,
    TrendsResponse trends,
    Map<String, ProviderBreakdownResponse> providerBreakdown
) {

    public static QueryMetricsResponse fromDomain(QueryMetricsSnapshot snapshot) {
        var breakdown = snapshot.providerBreakdown().stream()
            .collect(Collectors.toMap(
                QueryMetricsSnapshot.ProviderMetric::provider,
                metric -> new ProviderBreakdownResponse(
                    metric.queries(),
                    metric.cost(),
                    metric.successRate(),
                    metric.avgResponseTime()
                ),
                (left, right) -> left,
                LinkedHashMap::new
            ));

        return new QueryMetricsResponse(
            snapshot.totalQueries(),
            snapshot.totalCost(),
            snapshot.avgCostPerQuery(),
            snapshot.successRate(),
            snapshot.avgResponseTime(),
            new TrendsResponse(
                snapshot.trends().costTrend(),
                snapshot.trends().qualityTrend(),
                snapshot.trends().volumeTrend()
            ),
            Map.copyOf(breakdown)
        );
    }

    public record TrendsResponse(String costTrend, String qualityTrend, String volumeTrend) {
    }

    public record ProviderBreakdownResponse(
        long queries,
        BigDecimal cost,
        double successRate,
        long avgResponseTime
    ) {
    }
}
