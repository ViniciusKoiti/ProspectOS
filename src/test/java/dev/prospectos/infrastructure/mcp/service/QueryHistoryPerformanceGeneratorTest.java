package dev.prospectos.infrastructure.mcp.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QueryHistoryPerformanceGeneratorTest {

    private final QueryHistoryPerformanceGenerator generator = new QueryHistoryPerformanceGenerator();

    @Test
    void buildsHourlyPerformanceSeriesFromObservations() {
        Instant now = Instant.now();
        var observations = List.of(
            new QueryMetricsObservation("nominatim", "query a", now.minusSeconds(600), 900, true, 3, new BigDecimal("0.05")),
            new QueryMetricsObservation("nominatim", "query b", now.minusSeconds(1200), 1100, false, 0, new BigDecimal("0.00"))
        );

        var performance = generator.generate("nominatim", "response_time", observations);

        assertThat(performance.provider()).isEqualTo("nominatim");
        assertThat(performance.metric()).isEqualTo("response_time");
        assertThat(performance.dataPoints()).hasSize(24);
        assertThat(performance.summary()).containsKeys("average", "minimum", "maximum", "trend", "dataPoints");
    }

    @Test
    void calculatesDifferentMetricViews() {
        Instant now = Instant.now();
        var observations = List.of(
            new QueryMetricsObservation("nominatim", "query a", now.minusSeconds(600), 900, true, 3, new BigDecimal("0.05")),
            new QueryMetricsObservation("nominatim", "query b", now.minusSeconds(1200), 1100, false, 0, new BigDecimal("0.00"))
        );

        assertThat(generator.generate("nominatim", "success_rate", observations).summary()).containsKey("average");
        assertThat(generator.generate("nominatim", "cost_per_query", observations).summary()).containsKey("average");
        assertThat(generator.generate("nominatim", "throughput", observations).summary()).containsKey("average");
        assertThat(generator.generate("nominatim", "error_rate", observations).summary()).containsKey("average");
    }
}
