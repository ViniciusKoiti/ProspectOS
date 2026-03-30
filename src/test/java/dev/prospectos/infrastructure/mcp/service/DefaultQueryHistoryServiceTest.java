package dev.prospectos.infrastructure.mcp.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import dev.prospectos.api.mcp.QueryTimeWindow;

class DefaultQueryHistoryServiceTest {

    private final DefaultQueryHistoryService service = new DefaultQueryHistoryService();

    @Test
    void shouldGenerateQueryHistoryForRequestedWindow() {
        var history = service.getQueryHistory(QueryTimeWindow.ONE_HOUR, "nominatim");

        assertThat(history.timeWindow()).isEqualTo(QueryTimeWindow.ONE_HOUR);
        assertThat(history.provider()).isEqualTo("nominatim");
        assertThat(history.executions()).isNotEmpty();
        assertThat(history.aggregatedMetrics()).containsKeys(
            "totalQueries",
            "successfulQueries",
            "successRate",
            "totalCost",
            "averageResponseTime",
            "costPerQuery"
        );
    }

    @Test
    void shouldGenerateProviderPerformanceSummary() {
        var performance = service.getProviderPerformance("bing-maps", "response_time");

        assertThat(performance.provider()).isEqualTo("bing-maps");
        assertThat(performance.metric()).isEqualTo("response_time");
        assertThat(performance.dataPoints()).hasSize(24);
        assertThat(performance.summary()).containsKeys("average", "minimum", "maximum", "trend", "dataPoints");
    }

    @Test
    void shouldGenerateMarketAnalysis() {
        var analysis = service.getMarketAnalysis("brazil", "technology");

        assertThat(analysis.country()).isEqualTo("brazil");
        assertThat(analysis.industry()).isEqualTo("technology");
        assertThat(analysis.marketMetrics()).containsKeys("marketSize", "growthRate", "competitionLevel");
        assertThat(analysis.insights()).isNotEmpty();
        assertThat(analysis.competitors()).hasSize(3);
    }
}
