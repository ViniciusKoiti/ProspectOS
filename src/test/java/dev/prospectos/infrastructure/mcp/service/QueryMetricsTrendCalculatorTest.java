package dev.prospectos.infrastructure.mcp.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QueryMetricsTrendCalculatorTest {

    private final QueryMetricsTrendCalculator calculator = new QueryMetricsTrendCalculator();

    @Test
    void calculatesPositiveAndNegativeTrends() {
        Instant now = Instant.now();
        var current = List.of(
            new QueryMetricsObservation("nominatim", "query a", now, 500, true, 2, new BigDecimal("0.10")),
            new QueryMetricsObservation("nominatim", "query b", now, 700, true, 1, new BigDecimal("0.10"))
        );
        var previous = List.of(
            new QueryMetricsObservation("nominatim", "query c", now.minusSeconds(3600), 700, true, 1, new BigDecimal("0.05")),
            new QueryMetricsObservation("nominatim", "query d", now.minusSeconds(3600), 900, false, 0, new BigDecimal("0.05"))
        );

        var trends = calculator.calculate(current, previous);

        assertThat(trends.costTrend()).isEqualTo("+100%");
        assertThat(trends.qualityTrend()).isEqualTo("+100%");
        assertThat(trends.volumeTrend()).isEqualTo("+0%");
    }

    @Test
    void handlesEmptyPreviousWindow() {
        Instant now = Instant.now();
        var current = List.of(new QueryMetricsObservation("nominatim", "query a", now, 500, true, 2, new BigDecimal("0.10")));

        var trends = calculator.calculate(current, List.of());

        assertThat(trends.costTrend()).isEqualTo("+100%");
        assertThat(trends.qualityTrend()).isEqualTo("+100%");
        assertThat(trends.volumeTrend()).isEqualTo("+100%");
    }
}
