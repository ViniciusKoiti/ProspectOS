package dev.prospectos.infrastructure.mcp.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.junit.jupiter.api.Test;

import dev.prospectos.api.mcp.QueryTimeWindow;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryQueryMetricsServiceTest {

    @Test
    void aggregatesRecordedExecutionsFromRealSources() {
        Instant now = Instant.parse("2026-03-29T12:00:00Z");
        var observations = new ConcurrentLinkedDeque<QueryMetricsObservation>();
        observations.add(new QueryMetricsObservation("amazon-location", "restaurants in sao paulo", now.minusSeconds(600), 900, true, 3, new BigDecimal("0.11")));
        observations.add(new QueryMetricsObservation("vector-company", "restaurants in sao paulo", now.minusSeconds(1200), 120, true, 5, new BigDecimal("0.00")));
        observations.add(new QueryMetricsObservation("scraper", "restaurants in sao paulo", now.minusSeconds(1800), 1600, false, 0, new BigDecimal("0.01")));
        observations.add(new QueryMetricsObservation("amazon-location", "older request", now.minusSeconds(5400), 850, true, 2, new BigDecimal("0.07")));
        var service = new InMemoryQueryMetricsService(observations, Clock.fixed(now, ZoneOffset.UTC), new QueryMetricsCostEstimator(), new QueryMetricsSnapshotFactory());

        var snapshot = service.getMetrics(QueryTimeWindow.ONE_HOUR, null);

        assertThat(snapshot.totalQueries()).isEqualTo(3);
        assertThat(snapshot.totalCost()).hasToString("0.12");
        assertThat(snapshot.avgCostPerQuery()).hasToString("0.0400");
        assertThat(snapshot.successRate()).isEqualTo(0.6667);
        assertThat(snapshot.avgResponseTime()).isEqualTo(873L);
        assertThat(service.observations(QueryTimeWindow.ONE_HOUR, "amazon-location")).hasSize(1);
        assertThat(service.observedProviders(QueryTimeWindow.ONE_HOUR)).containsExactly("amazon-location", "scraper", "vector-company");
    }

    @Test
    void recordsExecutionsAndTrimsEntriesOutsideThirtyDayWindow() {
        Instant now = Instant.parse("2026-03-29T12:00:00Z");
        var observations = new ConcurrentLinkedDeque<QueryMetricsObservation>();
        observations.add(new QueryMetricsObservation("amazon-location", "stale request", now.minusSeconds(40L * 24L * 3600L), 700, true, 1, new BigDecimal("0.04")));
        var service = new InMemoryQueryMetricsService(observations, Clock.fixed(now, ZoneOffset.UTC), new QueryMetricsCostEstimator(), new QueryMetricsSnapshotFactory());

        service.recordExecution("amazon-location", "fresh request", 1000, true, 2);
        var snapshot = service.getMetrics(QueryTimeWindow.THIRTY_DAYS, "amazon-location");

        assertThat(snapshot.totalQueries()).isEqualTo(1);
        assertThat(snapshot.totalCost()).hasToString("0.07");
        assertThat(snapshot.providerBreakdown()).singleElement().satisfies(metric -> {
            assertThat(metric.provider()).isEqualTo("amazon-location");
            assertThat(metric.queries()).isEqualTo(1);
        });
    }
}
