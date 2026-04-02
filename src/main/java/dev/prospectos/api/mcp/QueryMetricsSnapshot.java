package dev.prospectos.api.mcp;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record QueryMetricsSnapshot(
    long totalQueries,
    BigDecimal totalCost,
    BigDecimal avgCostPerQuery,
    double successRate,
    long avgResponseTime,
    Trends trends,
    List<ProviderMetric> providerBreakdown
) {

    public QueryMetricsSnapshot {
        Objects.requireNonNull(totalCost, "totalCost must not be null");
        Objects.requireNonNull(avgCostPerQuery, "avgCostPerQuery must not be null");
        Objects.requireNonNull(trends, "trends must not be null");
        providerBreakdown = List.copyOf(providerBreakdown);
    }

    public record Trends(String costTrend, String qualityTrend, String volumeTrend) {
    }

    public record ProviderMetric(
        String provider,
        long queries,
        BigDecimal cost,
        double successRate,
        long avgResponseTime
    ) {
        public ProviderMetric {
            Objects.requireNonNull(provider, "provider must not be null");
            Objects.requireNonNull(cost, "cost must not be null");
        }
    }
}
