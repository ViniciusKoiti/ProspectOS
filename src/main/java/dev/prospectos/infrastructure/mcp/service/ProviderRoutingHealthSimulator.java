package dev.prospectos.infrastructure.mcp.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import dev.prospectos.api.mcp.ProviderHealth;
import dev.prospectos.api.mcp.QueryMetricsSnapshot;
import dev.prospectos.api.mcp.RoutingStrategy;

final class ProviderRoutingHealthSimulator {

    ProviderHealth generate(String provider, QueryMetricsSnapshot snapshot) {
        QueryMetricsSnapshot.ProviderMetric metric = snapshot.providerBreakdown().stream()
            .filter(candidate -> candidate.provider().equals(provider))
            .findFirst()
            .orElse(new QueryMetricsSnapshot.ProviderMetric(provider, 0, BigDecimal.ZERO, 0.0d, 0L));
        String status = status(metric);
        return new ProviderHealth(provider, status, metric.avgResponseTime(), 1.0d - metric.successRate(), lastError(status), recommendations(status, metric));
    }

    int estimatedSavings(RoutingStrategy strategy, QueryMetricsSnapshot snapshot) {
        double base = snapshot.avgCostPerQuery().doubleValue() * 1000.0d;
        return switch (strategy) {
            case COST_OPTIMIZED -> (int) Math.min(35, Math.round(base));
            case PERFORMANCE_OPTIMIZED -> 0;
            case BALANCED -> (int) Math.min(15, Math.round(base / 2.0d));
        };
    }

    long impactedQueriesPerHour(QueryMetricsSnapshot snapshot) {
        return snapshot.totalQueries();
    }

    private String status(QueryMetricsSnapshot.ProviderMetric metric) {
        if (metric.queries() == 0) {
            return "UNKNOWN";
        }
        if (metric.successRate() < 0.50d) {
            return "DOWN";
        }
        if (metric.successRate() < 0.85d || metric.avgResponseTime() > 2_500L) {
            return "DEGRADED";
        }
        return "HEALTHY";
    }

    private String lastError(String status) {
        return "DOWN".equals(status) ? "Provider is failing recent requests" : null;
    }

    private List<String> recommendations(String status, QueryMetricsSnapshot.ProviderMetric metric) {
        return switch (status) {
            case "HEALTHY" -> List.of("Provider is healthy for current routing", "Observed success rate: " + percent(metric.successRate()));
            case "DEGRADED" -> List.of("Reduce traffic until latency and failures stabilize", "Observed success rate: " + percent(metric.successRate()));
            case "DOWN" -> List.of("Remove provider from active routing", "Retry after provider health recovers");
            default -> List.of("Collect traffic before making routing decisions");
        };
    }

    private String percent(double value) {
        return BigDecimal.valueOf(value * 100.0d).setScale(0, RoundingMode.HALF_UP) + "%";
    }
}
