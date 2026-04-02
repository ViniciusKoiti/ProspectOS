package dev.prospectos.infrastructure.mcp.tools;

import dev.prospectos.api.mcp.ProviderRoutingService;
import dev.prospectos.api.mcp.RoutingStrategy;
import dev.prospectos.infrastructure.mcp.config.ConditionalOnMcpEnabled;
import dev.prospectos.infrastructure.mcp.dto.ProviderHealthResponse;
import dev.prospectos.infrastructure.mcp.dto.RoutingUpdateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnMcpEnabled
@RequiredArgsConstructor
public class ProviderRoutingMcpTools {

    private final ProviderRoutingService providerRoutingService;
    private final ProviderRoutingMcpInputParser inputParser;
    private final ProviderRoutingConfigurationTester configurationTester;

    @McpTool(name = "update_provider_routing", description = "Update provider routing strategy to optimize cost, performance, or balance both. AI agents use this to autonomously adjust routing based on metrics and business conditions.")
    public RoutingUpdateResponse updateProviderRouting(
        @McpToolParam(description = "Routing strategy: COST_OPTIMIZED, PERFORMANCE_OPTIMIZED, or BALANCED", required = true) String strategy,
        @McpToolParam(description = "Optional comma-separated provider priority list", required = false) String providerPriority,
        @McpToolParam(description = "Optional conditions as key=value pairs separated by commas", required = false) String conditions
    ) {
        var parsedStrategy = RoutingStrategy.fromValue(strategy);
        var parsedPriority = inputParser.parseProviderPriority(providerPriority);
        var parsedConditions = inputParser.parseConditions(conditions);
        log.debug("MCP update_provider_routing called with strategy={} priority={} conditions={}", parsedStrategy.name(), parsedPriority, parsedConditions);
        var response = RoutingUpdateResponse.fromDomain(providerRoutingService.updateRouting(parsedStrategy, parsedPriority, parsedConditions));
        log.info("Provider routing updated via MCP: strategy={} success={} savings={}%", parsedStrategy.name(), response.success(), response.estimatedSavingsPercent());
        return response;
    }

    @McpTool(name = "get_provider_health", description = "Get real-time health status of all location search providers including response times, error rates, and optimization recommendations for autonomous decision making.")
    public List<ProviderHealthResponse> getProviderHealth() {
        log.debug("MCP get_provider_health called");
        var responses = providerRoutingService.getProviderHealth().stream().map(ProviderHealthResponse::fromDomain).toList();
        var healthyCount = responses.stream().mapToInt(response -> "HEALTHY".equals(response.status()) ? 1 : 0).sum();
        log.info("Returning provider health via MCP: total={} healthy={} degraded/down={}", responses.size(), healthyCount, responses.size() - healthyCount);
        return responses;
    }

    @McpTool(name = "test_provider_configuration", description = "Test a provider configuration with sample queries to validate routing changes before applying them. Returns success rates and performance metrics.")
    public Map<String, Object> testProviderConfiguration(
        @McpToolParam(description = "Comma-separated list of providers to test", required = true) String providers
    ) {
        var providerList = inputParser.parseProviderPriority(providers);
        log.debug("MCP test_provider_configuration called with providers={}", providerList);
        var results = configurationTester.test(providerList);
        log.info("Provider configuration test completed: providers={} successRate={}", providerList, results.get("overallSuccessRate"));
        return results;
    }
}



