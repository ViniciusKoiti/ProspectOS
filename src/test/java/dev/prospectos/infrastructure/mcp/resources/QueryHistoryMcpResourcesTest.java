package dev.prospectos.infrastructure.mcp.resources;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.prospectos.api.mcp.CompetitorData;
import dev.prospectos.api.mcp.MarketAnalysisData;
import dev.prospectos.api.mcp.PerformanceDataPoint;
import dev.prospectos.api.mcp.ProviderPerformanceData;
import dev.prospectos.api.mcp.QueryExecution;
import dev.prospectos.api.mcp.QueryHistoryData;
import dev.prospectos.api.mcp.QueryHistoryService;
import dev.prospectos.api.mcp.QueryTimeWindow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class QueryHistoryMcpResourcesTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private QueryHistoryService queryHistoryService;

    @Test
    void shouldGetQueryHistoryWithValidParameters() throws Exception {
        when(queryHistoryService.getQueryHistory(QueryTimeWindow.ONE_HOUR, "nominatim")).thenReturn(createMockQueryHistoryData());
        var resources = new QueryHistoryMcpResources(queryHistoryService, new McpJsonResourceSerializer());

        var result = objectMapper.readTree(resources.getQueryHistory("1h", "nominatim"));

        assertThat(result.get("timeWindow").asText()).isEqualTo("1h");
        assertThat(result.get("provider").asText()).isEqualTo("nominatim");
        assertThat(result.get("executions")).hasSize(2);
        verify(queryHistoryService).getQueryHistory(QueryTimeWindow.ONE_HOUR, "nominatim");
    }

    @Test
    void shouldHandleAllProviderParameter() {
        when(queryHistoryService.getQueryHistory(QueryTimeWindow.TWENTY_FOUR_HOURS, null)).thenReturn(createMockQueryHistoryData());
        var resources = new QueryHistoryMcpResources(queryHistoryService, new McpJsonResourceSerializer());

        resources.getQueryHistory("24h", "all");

        verify(queryHistoryService).getQueryHistory(QueryTimeWindow.TWENTY_FOUR_HOURS, null);
    }

    @Test
    void shouldGetProviderPerformanceWithValidMetric() throws Exception {
        when(queryHistoryService.getProviderPerformance("nominatim", "response_time")).thenReturn(createMockProviderPerformanceData());
        var resources = new QueryHistoryMcpResources(queryHistoryService, new McpJsonResourceSerializer());

        var result = objectMapper.readTree(resources.getProviderPerformance("nominatim", "response-time"));

        assertThat(result.get("provider").asText()).isEqualTo("nominatim");
        assertThat(result.get("metric").asText()).isEqualTo("response_time");
        assertThat(result.get("dataPoints")).hasSize(2);
        verify(queryHistoryService).getProviderPerformance("nominatim", "response_time");
    }

    @Test
    void shouldValidateMetricParameter() {
        var resources = new QueryHistoryMcpResources(queryHistoryService, new McpJsonResourceSerializer());

        assertThatThrownBy(() -> resources.getProviderPerformance("nominatim", "invalid-metric"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid metric");
    }

    @Test
    void shouldRejectEmptyMetricParameter() {
        var resources = new QueryHistoryMcpResources(queryHistoryService, new McpJsonResourceSerializer());

        assertThatThrownBy(() -> resources.getProviderPerformance("nominatim", ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Metric parameter is required");
    }

    @Test
    void shouldGetMarketAnalysisWithValidParameters() throws Exception {
        when(queryHistoryService.getMarketAnalysis("brazil", "technology")).thenReturn(createMockMarketAnalysisData());
        var resources = new QueryHistoryMcpResources(queryHistoryService, new McpJsonResourceSerializer());

        var result = objectMapper.readTree(resources.getMarketAnalysis("brazil", "technology"));

        assertThat(result.get("country").asText()).isEqualTo("brazil");
        assertThat(result.get("industry").asText()).isEqualTo("technology");
        assertThat(result.get("marketMetrics").get("marketSize").asInt()).isEqualTo(5000000);
        verify(queryHistoryService).getMarketAnalysis("brazil", "technology");
    }

    @Test
    void shouldValidateCountryParameter() {
        var resources = new QueryHistoryMcpResources(queryHistoryService, new McpJsonResourceSerializer());

        assertThatThrownBy(() -> resources.getMarketAnalysis("", "technology"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Country parameter is required");
    }

    @Test
    void shouldValidateIndustryParameter() {
        var resources = new QueryHistoryMcpResources(queryHistoryService, new McpJsonResourceSerializer());

        assertThatThrownBy(() -> resources.getMarketAnalysis("brazil", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Industry parameter is required");
    }

    @Test
    void shouldAcceptAllValidMetrics() {
        when(queryHistoryService.getProviderPerformance(any(), any())).thenReturn(createMockProviderPerformanceData());
        var resources = new QueryHistoryMcpResources(queryHistoryService, new McpJsonResourceSerializer());

        assertThat(resources.getProviderPerformance("nominatim", "response_time")).isNotBlank();
        assertThat(resources.getProviderPerformance("nominatim", "success_rate")).isNotBlank();
        assertThat(resources.getProviderPerformance("nominatim", "cost_per_query")).isNotBlank();
        assertThat(resources.getProviderPerformance("nominatim", "throughput")).isNotBlank();
        assertThat(resources.getProviderPerformance("nominatim", "error_rate")).isNotBlank();

        verify(queryHistoryService, times(5)).getProviderPerformance(eq("nominatim"), any());
    }

    private QueryHistoryData createMockQueryHistoryData() {
        var executions = List.of(
            new QueryExecution("2023-12-01T10:00:00", "test query 1", "nominatim", true, 800L, 0.05, null),
            new QueryExecution("2023-12-01T10:05:00", "test query 2", "nominatim", false, 5000L, 0.0, "Timeout")
        );
        Map<String, Object> metrics = Map.of("totalQueries", 2, "successfulQueries", 1, "successRate", 0.5, "totalCost", 0.05, "averageResponseTime", 800.0, "costPerQuery", 0.025);
        return new QueryHistoryData(QueryTimeWindow.ONE_HOUR, "nominatim", executions, metrics);
    }

    private ProviderPerformanceData createMockProviderPerformanceData() {
        var dataPoints = List.of(
            new PerformanceDataPoint("2023-12-01T10:00:00", 800.0, Map.of("provider", "nominatim")),
            new PerformanceDataPoint("2023-12-01T11:00:00", 900.0, Map.of("provider", "nominatim"))
        );
        Map<String, Object> summary = Map.of("average", 850.0, "minimum", 800.0, "maximum", 900.0, "trend", "STABLE");
        return new ProviderPerformanceData("nominatim", "response_time", dataPoints, summary);
    }

    private MarketAnalysisData createMockMarketAnalysisData() {
        Map<String, Object> marketMetrics = Map.of("marketSize", 5000000, "growthRate", 0.15, "competitionLevel", 0.7);
        var insights = List.of("Market shows strong growth potential", "Competition is moderate");
        var competitors = Map.of(
            "Competitor A", new CompetitorData("Competitor A", 0.25, List.of("Strong brand"), List.of("High pricing")),
            "Competitor B", new CompetitorData("Competitor B", 0.15, List.of("Local expertise"), List.of("Limited tech"))
        );
        return new MarketAnalysisData("brazil", "technology", marketMetrics, insights, competitors);
    }
}
