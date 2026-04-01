package dev.prospectos.infrastructure.mcp.tools;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import dev.prospectos.api.mcp.QueryMetricsService;
import dev.prospectos.api.mcp.QueryTimeWindow;

@Component
class ProviderRoutingConfigurationTester {

    private final QueryMetricsService queryMetricsService;

    ProviderRoutingConfigurationTester(QueryMetricsService queryMetricsService) {
        this.queryMetricsService = queryMetricsService;
    }

    Map<String, Object> test(List<String> providers) {
        List<Map<String, Object>> providerResults = providers.stream().map(this::providerResult).toList();
        double successRate = providerResults.stream().mapToDouble(result -> (double) result.get("successRate")).average().orElse(0.0d);
        long averageResponseTime = Math.round(providerResults.stream().mapToLong(result -> (long) result.get("responseTime")).average().orElse(0.0d));
        Map<String, Object> results = new LinkedHashMap<>();
        results.put("testStatus", successRate >= 0.85d ? "PASSED" : successRate >= 0.50d ? "DEGRADED" : "FAILED");
        results.put("totalTests", providers.size());
        results.put("successfulTests", providerResults.stream().filter(result -> (double) result.get("successRate") >= 0.85d).count());
        results.put("averageResponseTime", averageResponseTime);
        results.put("overallSuccessRate", round(successRate));
        results.put("providerResults", toProviderMap(providers, providerResults));
        return results;
    }

    private Map<String, Object> providerResult(String provider) {
        var snapshot = queryMetricsService.getMetrics(QueryTimeWindow.ONE_HOUR, provider);
        var metric = snapshot.providerBreakdown().stream().findFirst().orElse(null);
        return Map.of(
            "responseTime", metric == null ? 0L : metric.avgResponseTime(),
            "successRate", metric == null ? 0.0d : round(metric.successRate()),
            "errors", metric == null ? 0 : Math.max(0, metric.queries() - (int) Math.round(metric.queries() * metric.successRate()))
        );
    }

    private Map<String, Object> toProviderMap(List<String> providers, List<Map<String, Object>> providerResults) {
        Map<String, Object> mapped = new LinkedHashMap<>();
        for (int index = 0; index < providers.size(); index++) {
            mapped.put(providers.get(index), providerResults.get(index));
        }
        return mapped;
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }
}
