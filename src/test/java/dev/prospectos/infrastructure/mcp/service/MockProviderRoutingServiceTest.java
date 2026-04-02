package dev.prospectos.infrastructure.mcp.service;

import dev.prospectos.api.mcp.RoutingStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MockProviderRoutingServiceTest {

    private final MockProviderRoutingService service = new MockProviderRoutingService();

    @Test
    void returnsProviderHealthSnapshots() {
        var health = service.getProviderHealth();

        assertThat(health).hasSize(3);
        assertThat(health.get(0).provider()).isEqualTo("google-places");
        assertThat(health.get(1).status()).isEqualTo("degraded");
        assertThat(health.get(2).recommendations()).contains("Safe for cost-sensitive broad searches.");
    }

    @Test
    void updatesRoutingState() {
        var result = service.updateRouting(
            RoutingStrategy.COST_OPTIMIZED,
            List.of("nominatim", "bing-maps", "google-places"),
            Map.of("budgetThreshold", "50.00")
        );

        assertThat(result.configurationApplied()).isTrue();
        assertThat(result.strategy()).isEqualTo(RoutingStrategy.COST_OPTIMIZED);
        assertThat(result.providerPriority()).containsExactly("nominatim", "bing-maps", "google-places");
        assertThat(result.summary()).contains("Applied COST_OPTIMIZED routing");
        assertThat(result.updatedAt()).isNotNull();
    }
}
