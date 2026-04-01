package dev.prospectos.infrastructure.mcp.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import dev.prospectos.api.mcp.ProviderHealth;
import dev.prospectos.api.mcp.ProviderRoutingService;
import dev.prospectos.api.mcp.QueryTimeWindow;
import dev.prospectos.api.mcp.RoutingStrategy;
import dev.prospectos.api.mcp.RoutingUpdate;
import dev.prospectos.infrastructure.mcp.config.ConditionalOnMcpEnabled;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnMcpEnabled
public class DefaultProviderRoutingService implements ProviderRoutingService {

    private final ObservedQueryMetricsService queryMetricsService;
    private final ProviderRoutingState state = new ProviderRoutingState();
    private final ProviderRoutingHealthSimulator evaluator = new ProviderRoutingHealthSimulator();

    public DefaultProviderRoutingService(ObservedQueryMetricsService queryMetricsService) {
        this.queryMetricsService = queryMetricsService;
    }

    @Override
    public RoutingUpdate updateRouting(RoutingStrategy strategy, List<String> providerPriority, Map<String, String> conditions) {
        var previousStrategy = state.strategy();
        var effectivePriority = providerPriority == null || providerPriority.isEmpty() ? state.providerPriority() : List.copyOf(providerPriority);
        state.update(strategy, effectivePriority);
        var hourlySnapshot = queryMetricsService.getMetrics(QueryTimeWindow.ONE_HOUR, null);
        log.info("Routing updated via MCP: {} -> {} priority={} conditions={}", previousStrategy, strategy, effectivePriority, conditions);
        return new RoutingUpdate(
            true,
            strategy,
            effectivePriority,
            "Routing strategy updated using observed provider metrics",
            Instant.now(),
            previousStrategy.name(),
            evaluator.estimatedSavings(strategy, hourlySnapshot),
            evaluator.impactedQueriesPerHour(hourlySnapshot),
            true
        );
    }

    @Override
    public List<ProviderHealth> getProviderHealth() {
        var snapshot = queryMetricsService.getMetrics(QueryTimeWindow.TWENTY_FOUR_HOURS, null);
        List<String> providers = new ArrayList<>(new LinkedHashSet<>(queryMetricsService.observedProviders(QueryTimeWindow.TWENTY_FOUR_HOURS)));
        state.providerPriority().stream().filter(provider -> !providers.contains(provider)).forEach(providers::add);
        return providers.stream().map(provider -> evaluator.generate(provider, snapshot)).toList();
    }
}




