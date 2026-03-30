package dev.prospectos.infrastructure.mcp.tools;

import dev.prospectos.api.mcp.QueryMetricsService;
import dev.prospectos.api.mcp.QueryMetricsSnapshot;
import dev.prospectos.api.mcp.QueryTimeWindow;
import dev.prospectos.infrastructure.service.compliance.AllowedSourcesProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryMetricsMcpToolsTest {

    @Mock
    private QueryMetricsService queryMetricsService;

    @Test
    void delegatesToServiceAndReturnsMappedResponse() {
        var tools = new QueryMetricsMcpTools(
            queryMetricsService,
            new AllowedSourcesProperties(List.of("amazon-location", "vector-company", "open-cnpj"), List.of("amazon-location"))
        );
        var snapshot = new QueryMetricsSnapshot(
            25,
            new BigDecimal("7.50"),
            new BigDecimal("0.3000"),
            0.94,
            820,
            new QueryMetricsSnapshot.Trends("+3%", "+1%", "+8%"),
            List.of(new QueryMetricsSnapshot.ProviderMetric("amazon-location", 25, new BigDecimal("7.50"), 0.94, 820))
        );

        when(queryMetricsService.getMetrics(QueryTimeWindow.TWENTY_FOUR_HOURS, "amazon-location")).thenReturn(snapshot);

        var response = tools.getQueryMetrics("24h", "AMAZON-LOCATION");

        assertThat(response.totalQueries()).isEqualTo(25);
        assertThat(response.providerBreakdown()).containsKey("amazon-location");
        verify(queryMetricsService).getMetrics(QueryTimeWindow.TWENTY_FOUR_HOURS, "amazon-location");
    }

    @Test
    void rejectsUnsupportedProvider() {
        var tools = new QueryMetricsMcpTools(
            queryMetricsService,
            new AllowedSourcesProperties(List.of("amazon-location", "vector-company"), List.of("amazon-location"))
        );

        assertThatThrownBy(() -> tools.getQueryMetrics("24h", "mapbox"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid provider 'mapbox'. Allowed values: amazon-location, vector-company, scraper");
    }
}