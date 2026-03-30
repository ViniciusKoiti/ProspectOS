package dev.prospectos.infrastructure.mcp.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import dev.prospectos.api.mcp.QueryTimeWindow;

class DefaultQueryMetricsServiceTest {

    private final DefaultQueryMetricsService service = new DefaultQueryMetricsService();

    @Test
    void shouldGenerateMetricsForAllProviders() {
        var snapshot = service.getMetrics(QueryTimeWindow.TWENTY_FOUR_HOURS, null);

        assertThat(snapshot.totalQueries()).isPositive();
        assertThat(snapshot.totalCost()).isPositive();
        assertThat(snapshot.avgCostPerQuery()).isPositive();
        assertThat(snapshot.successRate()).isBetween(0.85, 0.97);
        assertThat(snapshot.avgResponseTime()).isBetween(800L, 1199L);
        assertThat(snapshot.providerBreakdown()).isNotEmpty();
    }

    @Test
    void shouldGenerateMetricsForSingleProviderFilter() {
        var snapshot = service.getMetrics(QueryTimeWindow.ONE_HOUR, "scraper");

        assertThat(snapshot.providerBreakdown()).singleElement().satisfies(metric -> {
            assertThat(metric.provider()).isEqualTo("scraper");
            assertThat(metric.queries()).isPositive();
            assertThat(metric.cost()).isPositive();
            assertThat(metric.successRate()).isBetween(0.8, 0.98);
            assertThat(metric.avgResponseTime()).isBetween(600L, 1399L);
        });
    }
}

