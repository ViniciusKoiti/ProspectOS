package dev.prospectos.infrastructure.mcp.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.junit.jupiter.api.Test;

import dev.prospectos.api.mcp.RoutingStrategy;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultProviderRoutingServiceTest {

    @Test
    void shouldUpdateRoutingUsingObservedHourlyMetrics() {
        var service = service();

        var update = service.updateRouting(RoutingStrategy.COST_OPTIMIZED, List.of("vector-company", "nominatim"), Map.of("region", "br"));

        assertThat(update.success()).isTrue();
        assertThat(update.previousStrategy()).isEqualTo(RoutingStrategy.BALANCED.name());
        assertThat(update.newStrategy()).isEqualTo(RoutingStrategy.COST_OPTIMIZED.name());
        assertThat(update.providerPriority()).containsExactly("vector-company", "nominatim");
        assertThat(update.estimatedSavingsPercent()).isGreaterThanOrEqualTo(0);
        assertThat(update.impactedQueriesPerHour()).isEqualTo(4L);
    }

    @Test
    void shouldExposeProviderHealthFromObservedTraffic() {
        var service = service();
        service.updateRouting(RoutingStrategy.BALANCED, List.of("vector-company", "nominatim", "scraper"), Map.of());

        var health = service.getProviderHealth();

        assertThat(health).extracting("provider").contains("nominatim", "vector-company", "scraper");
        assertThat(health).filteredOn(provider -> provider.provider().equals("nominatim")).singleElement().satisfies(provider -> {
            assertThat(provider.status()).isEqualTo("DEGRADED");
            assertThat(provider.recommendations()).isNotEmpty();
        });
        assertThat(health).filteredOn(provider -> provider.provider().equals("vector-company")).singleElement().satisfies(provider -> {
            assertThat(provider.status()).isEqualTo("HEALTHY");
            assertThat(provider.errorRate()).isEqualTo(0.0d);
        });
    }

    private DefaultProviderRoutingService service() {
        Instant now = Instant.parse("2026-03-29T12:00:00Z");
        var observations = new ConcurrentLinkedDeque<QueryMetricsObservation>();
        observations.add(new QueryMetricsObservation("nominatim", "tech companies in brazil", now.minusSeconds(300), 900, true, 3, BigDecimal.ZERO));
        observations.add(new QueryMetricsObservation("nominatim", "tech companies in brazil", now.minusSeconds(600), 2900, false, 0, BigDecimal.ZERO));
        observations.add(new QueryMetricsObservation("vector-company", "tech companies in brazil", now.minusSeconds(900), 140, true, 4, BigDecimal.ZERO));
        observations.add(new QueryMetricsObservation("vector-company", "tech companies in brazil", now.minusSeconds(1200), 120, true, 2, BigDecimal.ZERO));
        var metricsService = new InMemoryQueryMetricsService(observations, Clock.fixed(now, ZoneOffset.UTC), new QueryMetricsCostEstimator(), new QueryMetricsSnapshotFactory());
        return new DefaultProviderRoutingService(metricsService);
    }
}
