package dev.prospectos.infrastructure.mcp.resources;

import dev.prospectos.api.mcp.QueryHistoryService;
import dev.prospectos.api.mcp.QueryTimeWindow;
import dev.prospectos.infrastructure.mcp.dto.MarketAnalysisResponse;
import dev.prospectos.infrastructure.mcp.dto.ProviderPerformanceResponse;
import dev.prospectos.infrastructure.mcp.dto.QueryHistoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Slf4j
@Component
@Profile("mcp")
@RequiredArgsConstructor
public class QueryHistoryMcpResources {

    private static final Set<String> VALID_METRICS = Set.of("response_time", "success_rate", "cost_per_query", "throughput", "error_rate");
    private final QueryHistoryService queryHistoryService;

    @McpResource(uri = "query-history://{timeWindow}/{provider}")
    public QueryHistoryResponse getQueryHistory(String timeWindow, String provider) {
        var parsedTimeWindow = QueryTimeWindow.fromValue(timeWindow);
        var normalizedProvider = normalizeProvider(provider);
        log.debug("MCP resource access: query-history timeWindow={} provider={}", timeWindow, provider);
        var response = QueryHistoryResponse.fromDomain(queryHistoryService.getQueryHistory(parsedTimeWindow, normalizedProvider));
        log.info("Query history resource accessed: timeWindow={} provider={} executions={}", parsedTimeWindow.value(), normalizedProvider, response.executions().size());
        return response;
    }

    @McpResource(uri = "provider-performance://{provider}/{metric}")
    public ProviderPerformanceResponse getProviderPerformance(String provider, String metric) {
        var normalizedProvider = normalizeProvider(provider);
        var normalizedMetric = required(metric, "Metric").replace("-", "_");
        if (!VALID_METRICS.contains(normalizedMetric)) {
            throw new IllegalArgumentException("Invalid metric: " + metric + ". Allowed values: " + String.join(", ", VALID_METRICS));
        }
        log.debug("MCP resource access: provider-performance provider={} metric={}", provider, metric);
        var response = ProviderPerformanceResponse.fromDomain(queryHistoryService.getProviderPerformance(normalizedProvider, normalizedMetric));
        log.info("Provider performance resource accessed: provider={} metric={} dataPoints={}", normalizedProvider, normalizedMetric, response.dataPoints().size());
        return response;
    }

    @McpResource(uri = "market-analysis://{country}/{industry}")
    public MarketAnalysisResponse getMarketAnalysis(String country, String industry) {
        var normalizedCountry = required(country, "Country");
        var normalizedIndustry = required(industry, "Industry");
        log.debug("MCP resource access: market-analysis country={} industry={}", country, industry);
        var response = MarketAnalysisResponse.fromDomain(queryHistoryService.getMarketAnalysis(normalizedCountry, normalizedIndustry));
        log.info("Market analysis resource accessed: country={} industry={} competitors={}", normalizedCountry, normalizedIndustry, response.competitors().size());
        return response;
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
