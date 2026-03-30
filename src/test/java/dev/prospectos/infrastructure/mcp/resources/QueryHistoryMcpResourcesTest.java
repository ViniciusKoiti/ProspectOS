package dev.prospectos.infrastructure.mcp.resources;

import dev.prospectos.api.mcp.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class QueryHistoryMcpResourcesTest {

    @Mock
    private QueryHistoryService queryHistoryService;

    @InjectMocks
    private QueryHistoryMcpResources queryHistoryMcpResources;

    @Test
    void shouldGetQueryHistoryWithValidParameters() {
        // Given
        var mockHistoryData = createMockQueryHistoryData();
        when(queryHistoryService.getQueryHistory(QueryTimeWindow.ONE_HOUR, "nominatim"))
            .thenReturn(mockHistoryData);

        // When
        var result = queryHistoryMcpResources.getQueryHistory("1h", "nominatim");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.timeWindow()).isEqualTo("1h");
        assertThat(result.provider()).isEqualTo("nominatim");
        assertThat(result.executions()).hasSize(2);
        assertThat(result.aggregatedMetrics()).containsKey("totalQueries");
        
        verify(queryHistoryService).getQueryHistory(QueryTimeWindow.ONE_HOUR, "nominatim");
    }

    @Test
    void shouldHandleAllProviderParameter() {
        // Given
        var mockHistoryData = createMockQueryHistoryData();
        when(queryHistoryService.getQueryHistory(QueryTimeWindow.TWENTY_FOUR_HOURS, null))
            .thenReturn(mockHistoryData);

        // When
        var result = queryHistoryMcpResources.getQueryHistory("24h", "all");

        // Then
        assertThat(result).isNotNull();
        verify(queryHistoryService).getQueryHistory(QueryTimeWindow.TWENTY_FOUR_HOURS, null);
    }

    @Test
    void shouldGetProviderPerformanceWithValidMetric() {
        // Given
        var mockPerformanceData = createMockProviderPerformanceData();
        when(queryHistoryService.getProviderPerformance("nominatim", "response_time"))
            .thenReturn(mockPerformanceData);

        // When
        var result = queryHistoryMcpResources.getProviderPerformance("nominatim", "response-time");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.provider()).isEqualTo("nominatim");
        assertThat(result.metric()).isEqualTo("response_time");
        assertThat(result.dataPoints()).hasSize(2);
        assertThat(result.summary()).containsKey("average");
        
        verify(queryHistoryService).getProviderPerformance("nominatim", "response_time");
    }

    @Test
    void shouldValidateMetricParameter() {
        // When & Then
        assertThatThrownBy(() -> queryHistoryMcpResources.getProviderPerformance("nominatim", "invalid-metric"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid metric");
    }

    @Test
    void shouldRejectEmptyMetricParameter() {
        // When & Then
        assertThatThrownBy(() -> queryHistoryMcpResources.getProviderPerformance("nominatim", ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Metric parameter is required");
    }

    @Test
    void shouldGetMarketAnalysisWithValidParameters() {
        // Given
        var mockAnalysisData = createMockMarketAnalysisData();
        when(queryHistoryService.getMarketAnalysis("brazil", "technology"))
            .thenReturn(mockAnalysisData);

        // When
        var result = queryHistoryMcpResources.getMarketAnalysis("brazil", "technology");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.country()).isEqualTo("brazil");
        assertThat(result.industry()).isEqualTo("technology");
        assertThat(result.marketMetrics()).containsKey("marketSize");
        assertThat(result.insights()).hasSize(2);
        assertThat(result.competitors()).hasSize(2);
        
        verify(queryHistoryService).getMarketAnalysis("brazil", "technology");
    }

    @Test
    void shouldValidateCountryParameter() {
        // When & Then
        assertThatThrownBy(() -> queryHistoryMcpResources.getMarketAnalysis("", "technology"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Country parameter is required");
    }

    @Test
    void shouldValidateIndustryParameter() {
        // When & Then
        assertThatThrownBy(() -> queryHistoryMcpResources.getMarketAnalysis("brazil", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Industry parameter is required");
    }

    @Test
    void shouldAcceptAllValidMetrics() {
        // Given
        var mockPerformanceData = createMockProviderPerformanceData();
        when(queryHistoryService.getProviderPerformance(any(), any())).thenReturn(mockPerformanceData);

        // When & Then - All should succeed
        assertThat(queryHistoryMcpResources.getProviderPerformance("nominatim", "response_time")).isNotNull();
        assertThat(queryHistoryMcpResources.getProviderPerformance("nominatim", "success_rate")).isNotNull();
        assertThat(queryHistoryMcpResources.getProviderPerformance("nominatim", "cost_per_query")).isNotNull();
        assertThat(queryHistoryMcpResources.getProviderPerformance("nominatim", "throughput")).isNotNull();
        assertThat(queryHistoryMcpResources.getProviderPerformance("nominatim", "error_rate")).isNotNull();

        verify(queryHistoryService, times(5)).getProviderPerformance(eq("nominatim"), any());
    }

    @Test
    void shouldNormalizeMetricNames() {
        // Given
        var mockPerformanceData = createMockProviderPerformanceData();
        when(queryHistoryService.getProviderPerformance("nominatim", "response_time"))
            .thenReturn(mockPerformanceData);

        // When
        var result = queryHistoryMcpResources.getProviderPerformance("nominatim", "response-time");

        // Then
        assertThat(result).isNotNull();
        verify(queryHistoryService).getProviderPerformance("nominatim", "response_time");
    }

    private QueryHistoryData createMockQueryHistoryData() {
        var executions = List.of(
            new QueryExecution("2023-12-01T10:00:00", "test query 1", "nominatim", true, 800L, 0.05, null),
            new QueryExecution("2023-12-01T10:05:00", "test query 2", "nominatim", false, 5000L, 0.0, "Timeout")
        );
        
        Map<String, Object> metrics = Map.of(
            "totalQueries", 2,
            "successfulQueries", 1,
            "successRate", 0.5,
            "totalCost", 0.05,
            "averageResponseTime", 800.0,
            "costPerQuery", 0.025
        );

        return new QueryHistoryData(QueryTimeWindow.ONE_HOUR, "nominatim", executions, metrics);
    }

    private ProviderPerformanceData createMockProviderPerformanceData() {
        var dataPoints = List.of(
            new PerformanceDataPoint("2023-12-01T10:00:00", 800.0, Map.of("provider", "nominatim")),
            new PerformanceDataPoint("2023-12-01T11:00:00", 900.0, Map.of("provider", "nominatim"))
        );

        Map<String, Object> summary = Map.of(
            "average", 850.0,
            "minimum", 800.0,
            "maximum", 900.0,
            "trend", "STABLE"
        );

        return new ProviderPerformanceData("nominatim", "response_time", dataPoints, summary);
    }

    private MarketAnalysisData createMockMarketAnalysisData() {
        Map<String, Object> marketMetrics = Map.of(
            "marketSize", 5000000,
            "growthRate", 0.15,
            "competitionLevel", 0.7
        );

        var insights = List.of(
            "Market shows strong growth potential",
            "Competition is moderate"
        );

        var competitors = Map.of(
            "Competitor A", new CompetitorData("Competitor A", 0.25, List.of("Strong brand"), List.of("High pricing")),
            "Competitor B", new CompetitorData("Competitor B", 0.15, List.of("Local expertise"), List.of("Limited tech"))
        );

        return new MarketAnalysisData("brazil", "technology", marketMetrics, insights, competitors);
    }
}
