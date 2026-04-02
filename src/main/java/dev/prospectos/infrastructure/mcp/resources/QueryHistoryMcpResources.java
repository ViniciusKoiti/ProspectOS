package dev.prospectos.infrastructure.mcp.resources;

import java.util.Locale;
import java.util.Set;

import dev.prospectos.infrastructure.mcp.config.ConditionalOnMcpEnabled;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springframework.stereotype.Component;

import dev.prospectos.api.mcp.QueryHistoryService;
import dev.prospectos.api.mcp.QueryTimeWindow;
import dev.prospectos.infrastructure.mcp.dto.MarketAnalysisResponse;
import dev.prospectos.infrastructure.mcp.dto.ProviderPerformanceResponse;
import dev.prospectos.infrastructure.mcp.dto.QueryHistoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnMcpEnabled
@RequiredArgsConstructor
public class QueryHistoryMcpResources {

    private static final Set<String> VALID_METRICS = Set.of("response_time", "success_rate", "cost_per_query", "throughput", "error_rate");

    private final QueryHistoryService queryHistoryService;
    private final McpJsonResourceSerializer serializer;

    @McpResource(uri = "query-history://{timeWindow}/{provider}")
    public String getQueryHistory(String timeWindow, String provider) {
        var parsedTimeWindow = QueryTimeWindow.fromValue(timeWindow);
        var normalizedProvider = normalizeProvider(provider);
        var response = QueryHistoryResponse.fromDomain(queryHistoryService.getQueryHistory(parsedTimeWindow, normalizedProvider));
        log.info("Query history resource accessed: timeWindow={} provider={} executions={}", parsedTimeWindow.value(), normalizedProvider, response.executions().size());
        return serializer.serialize(response);
    }

    @McpResource(uri = "provider-performance://{provider}/{metric}")
    public String getProviderPerformance(String provider, String metric) {
        var normalizedProvider = normalizeProvider(provider);
        var normalizedMetric = required(metric, "Metric").replace("-", "_");
        if (!VALID_METRICS.contains(normalizedMetric)) {
            throw new IllegalArgumentException("Invalid metric: " + metric + ". Allowed values: " + String.join(", ", VALID_METRICS));
        }
        var response = ProviderPerformanceResponse.fromDomain(queryHistoryService.getProviderPerformance(normalizedProvider, normalizedMetric));
        log.info("Provider performance resource accessed: provider={} metric={} dataPoints={}", normalizedProvider, normalizedMetric, response.dataPoints().size());
        return serializer.serialize(response);
    }

    @McpResource(uri = "market-analysis://{country}/{industry}")
    public String getMarketAnalysis(String country, String industry) {
        var normalizedCountry = required(country, "Country");
        var normalizedIndustry = required(industry, "Industry");
        var response = MarketAnalysisResponse.fromDomain(queryHistoryService.getMarketAnalysis(normalizedCountry, normalizedIndustry));
        log.info("Market analysis resource accessed: country={} industry={} competitors={}", normalizedCountry, normalizedIndustry, response.competitors().size());
        return serializer.serialize(response);
    }

    private String normalizeProvider(String provider) {
        return provider == null || provider.trim().isEmpty() || "all".equalsIgnoreCase(provider.trim()) ? null : provider.trim().toLowerCase(Locale.ROOT);
    }

    private String required(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " parameter is required");
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}



