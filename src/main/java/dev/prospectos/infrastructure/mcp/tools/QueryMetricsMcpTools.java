package dev.prospectos.infrastructure.mcp.tools;

import dev.prospectos.api.mcp.QueryMetricsService;
import dev.prospectos.api.mcp.QueryTimeWindow;
import dev.prospectos.infrastructure.mcp.config.ConditionalOnMcpEnabled;
import dev.prospectos.infrastructure.mcp.dto.QueryMetricsResponse;
import dev.prospectos.infrastructure.service.compliance.AllowedSourcesProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Component
@ConditionalOnMcpEnabled
@RequiredArgsConstructor
public class QueryMetricsMcpTools {

    private final QueryMetricsService queryMetricsService;
    private final AllowedSourcesProperties allowedSourcesProperties;

    @McpTool(
        name = "get_query_metrics",
        description = "Return query volume, cost, success rate, response times, and source mix so an AI agent can "
            + "decide whether to optimize routing, budget usage, or investigate source incidents."
    )
    public QueryMetricsResponse getQueryMetrics(
        @McpToolParam(
            description = "Aggregation window for the metrics. Allowed values: 1h, 24h, 7d, 30d. Use 1h for active "
                + "incidents, 24h for daily operations, and 7d or 30d for trend analysis.",
            required = true
        )
        String timeWindow,
        @McpToolParam(
            description = "Optional source filter. Use one of the configured lead sources such as in-memory, "
                + "vector-company, llm-discovery, cnpj-ws, open-cnpj, amazon-location, or scraper.",
            required = false
        )
        String provider
    ) {
        var parsedTimeWindow = QueryTimeWindow.fromValue(timeWindow);
        var normalizedProvider = normalizeProvider(provider);

        log.debug("MCP get_query_metrics called with timeWindow={} provider={}", parsedTimeWindow.value(), normalizedProvider);

        var snapshot = queryMetricsService.getMetrics(parsedTimeWindow, normalizedProvider);
        var response = QueryMetricsResponse.fromDomain(snapshot);

        log.info(
            "Returning MCP metrics for window={} provider={} totalQueries={} totalCost={}",
            parsedTimeWindow.value(),
            normalizedProvider == null ? "all" : normalizedProvider,
            response.totalQueries(),
            response.totalCost()
        );

        return response;
    }

    private String normalizeProvider(String provider) {
        if (!StringUtils.hasText(provider)) {
            return null;
        }

        var normalizedProvider = provider.trim().toLowerCase(Locale.ROOT);
        var supportedProviders = supportedProviders();
        if (!supportedProviders.contains(normalizedProvider)) {
            throw new IllegalArgumentException(
                "Invalid provider '%s'. Allowed values: %s".formatted(provider, String.join(", ", supportedProviders))
            );
        }

        return normalizedProvider;
    }

    private Set<String> supportedProviders() {
        var providers = new LinkedHashSet<String>();
        if (allowedSourcesProperties.allowedSources() != null) {
            allowedSourcesProperties.allowedSources().stream()
                .filter(StringUtils::hasText)
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .forEach(providers::add);
        }
        providers.add("scraper");
        return Collections.unmodifiableSet(providers);
    }
}




