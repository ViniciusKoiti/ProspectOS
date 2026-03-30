package dev.prospectos.infrastructure.mcp.service;

import dev.prospectos.api.mcp.ProviderRoutingService;
import dev.prospectos.api.mcp.RoutingStrategy;
import dev.prospectos.api.mcp.RoutingUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
@Profile("mcp")
public class DefaultProviderRoutingService implements ProviderRoutingService {

    private static final List<String> MOCK_PROVIDERS = List.of("nominatim", "bing-maps", "google-places", "scraper", "llm-discovery");

    private final ProviderRoutingHealthSimulator simulator = new ProviderRoutingHealthSimulator(new Random());
    private RoutingStrategy currentStrategy = RoutingStrategy.BALANCED;
    private List<String> currentPriority = List.of("nominatim", "bing-maps", "google-places");

    @Override
    public RoutingUpdate updateRouting(RoutingStrategy strategy, List<String> providerPriority, Map<String, String> conditions) {
        log.debug("Updating routing strategy={} priority={} conditions={}", strategy, providerPriority, conditions);
        var previousStrategy = currentStrategy;
        currentStrategy = strategy;
        currentPriority = providerPriority != null ? List.copyOf(providerPriority) : currentPriority;
        var update = new RoutingUpdate(true, "Routing strategy updated successfully", previousStrategy.name(), strategy.name(), simulator.estimatedSavings(strategy), simulator.impactedQueriesPerHour(), true);
        log.info("Routing updated: {} -> {}, estimated savings: {}%, impacted queries: {}", previousStrategy, strategy, update.estimatedSavingsPercent(), update.impactedQueriesPerHour());
        return update;
    }

    @Override
    public List<dev.prospectos.api.mcp.ProviderHealth> getProviderHealth() {
        log.debug("Generating provider health status for {} providers", MOCK_PROVIDERS.size());
        return MOCK_PROVIDERS.stream().map(simulator::generate).toList();
    }
}
