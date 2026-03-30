package dev.prospectos.infrastructure.mcp.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import dev.prospectos.api.mcp.RoutingStrategy;

class DefaultProviderRoutingServiceTest {

    private final DefaultProviderRoutingService service = new DefaultProviderRoutingService();

    @Test
    void shouldUpdateRoutingAndExposeTransitionMetadata() {
        var update = service.updateRouting(
            RoutingStrategy.COST_OPTIMIZED,
            List.of("bing-maps", "nominatim"),
            Map.of("region", "br")
        );

        assertThat(update.success()).isTrue();
        assertThat(update.message()).contains("updated successfully");
        assertThat(update.previousStrategy()).isEqualTo(RoutingStrategy.BALANCED.name());
        assertThat(update.newStrategy()).isEqualTo(RoutingStrategy.COST_OPTIMIZED.name());
        assertThat(update.estimatedSavingsPercent()).isBetween(15, 34);
        assertThat(update.impactedQueriesPerHour()).isBetween(50L, 249L);
        assertThat(update.rollbackPossible()).isTrue();
    }

    @Test
    void shouldExposeHealthForKnownProviders() {
        var health = service.getProviderHealth();

        assertThat(health).hasSize(5);
        assertThat(health).allSatisfy(provider -> {
            assertThat(provider.provider()).isNotBlank();
            assertThat(provider.status()).isIn("HEALTHY", "DEGRADED", "DOWN");
            assertThat(provider.responseTime()).isPositive();
            assertThat(provider.errorRate()).isBetween(0.0, 1.0);
            assertThat(provider.recommendations()).isNotEmpty();
        });
    }
}

