package dev.prospectos.infrastructure.mcp.service;

import dev.prospectos.api.mcp.ProviderHealth;
import dev.prospectos.api.mcp.ProviderRoutingService;
import dev.prospectos.api.mcp.RoutingStrategy;
import dev.prospectos.api.mcp.RoutingUpdate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Profile("mcp-mock")
public class MockProviderRoutingService implements ProviderRoutingService {

    private final AtomicReference<RoutingUpdate> currentRouting = new AtomicReference<>(
        new RoutingUpdate(
            true,
            RoutingStrategy.BALANCED,
            List.of("google-places", "bing-maps", "nominatim"),
            "Default balanced routing loaded for MCP operations.",
            Instant.now()
        )
    );

    @Override
    public RoutingUpdate updateRouting(RoutingStrategy strategy, List<String> providerPriority, Map<String, String> conditions) {
        var summary = "Applied %s routing with %d priority entries and %d conditions."
            .formatted(strategy.name(), providerPriority.size(), conditions.size());
        var update = new RoutingUpdate(true, strategy, providerPriority, summary, Instant.now());
        currentRouting.set(update);
        return update;
    }

    @Override
    public List<ProviderHealth> getProviderHealth() {
        return List.of(
            new ProviderHealth(
                "google-places",
                "healthy",
                880,
                0.021,
                null,
                List.of("Keep as primary provider for high-quality searches.")
            ),
            new ProviderHealth(
                "bing-maps",
                "degraded",
                1420,
                0.081,
                "Recent rate limiting spikes during peak hours.",
                List.of("Use as fallback during business hours.", "Monitor throughput before promoting to primary.")
            ),
            new ProviderHealth(
                "nominatim",
                "healthy",
                1760,
                0.064,
                null,
                List.of("Safe for cost-sensitive broad searches.")
            )
        );
    }
}

