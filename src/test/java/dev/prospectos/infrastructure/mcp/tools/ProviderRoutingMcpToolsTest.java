package dev.prospectos.infrastructure.mcp.tools;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import dev.prospectos.api.mcp.ProviderHealth;
import dev.prospectos.api.mcp.ProviderRoutingService;
import dev.prospectos.api.mcp.QueryMetricsService;
import dev.prospectos.api.mcp.QueryMetricsSnapshot;
import dev.prospectos.api.mcp.QueryTimeWindow;
import dev.prospectos.api.mcp.RoutingStrategy;
import dev.prospectos.api.mcp.RoutingUpdate;
import dev.prospectos.infrastructure.service.compliance.AllowedSourcesProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ProviderRoutingMcpToolsTest {

    @Mock
    private ProviderRoutingService providerRoutingService;

    @Mock
    private AllowedSourcesProperties allowedSourcesProperties;

    private ProviderRoutingMcpTools providerRoutingMcpTools;

    @BeforeEach
    void setUp() {
        providerRoutingMcpTools = new ProviderRoutingMcpTools(
            providerRoutingService,
            new ProviderRoutingMcpInputParser(allowedSourcesProperties),
            new ProviderRoutingConfigurationTester(queryMetricsService())
        );
    }

    @Test
    void shouldUpdateRoutingWithValidStrategy() {
        when(providerRoutingService.updateRouting(eq(RoutingStrategy.COST_OPTIMIZED), any(), any())).thenReturn(createMockRoutingUpdate());
        when(allowedSourcesProperties.allowedSources()).thenReturn(List.of("nominatim", "bing-maps"));

        var result = providerRoutingMcpTools.updateProviderRouting("COST_OPTIMIZED", "nominatim,bing-maps", "max_cost=0.01,min_success_rate=0.95");

        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();
        assertThat(result.newStrategy()).isEqualTo("COST_OPTIMIZED");
        assertThat(result.estimatedSavingsPercent()).isEqualTo(25);
        verify(providerRoutingService).updateRouting(eq(RoutingStrategy.COST_OPTIMIZED), eq(List.of("nominatim", "bing-maps")), any(Map.class));
    }

    @Test
    void shouldValidateRoutingStrategy() {
        assertThatThrownBy(() -> providerRoutingMcpTools.updateProviderRouting("INVALID_STRATEGY", null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid routing strategy");
    }

    @Test
    void shouldValidateProviderPriority() {
        when(allowedSourcesProperties.allowedSources()).thenReturn(List.of("nominatim", "bing-maps"));

        assertThatThrownBy(() -> providerRoutingMcpTools.updateProviderRouting("BALANCED", "invalid-provider", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid providers");
    }

    @Test
    void shouldReturnProviderHealthStatus() {
        when(providerRoutingService.getProviderHealth()).thenReturn(List.of(createMockProviderHealth("nominatim", "HEALTHY"), createMockProviderHealth("bing-maps", "DEGRADED")));

        var result = providerRoutingMcpTools.getProviderHealth();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).provider()).isEqualTo("nominatim");
        assertThat(result.get(0).status()).isEqualTo("HEALTHY");
        assertThat(result.get(1).provider()).isEqualTo("bing-maps");
        assertThat(result.get(1).status()).isEqualTo("DEGRADED");
        verify(providerRoutingService).getProviderHealth();
    }

    @Test
    void shouldTestProviderConfiguration() {
        when(allowedSourcesProperties.allowedSources()).thenReturn(List.of("nominatim", "bing-maps"));

        var result = providerRoutingMcpTools.testProviderConfiguration("nominatim,bing-maps");

        assertThat(result).containsKeys("testStatus", "totalTests", "successfulTests", "averageResponseTime", "overallSuccessRate", "providerResults");
        assertThat(result.get("testStatus")).isEqualTo("PASSED");
        assertThat(result.get("totalTests")).isEqualTo(2);
        @SuppressWarnings("unchecked")
        var providerResults = (Map<String, Map<String, Object>>) result.get("providerResults");
        assertThat(providerResults).containsKeys("nominatim", "bing-maps");
    }

    @Test
    void shouldHandleEmptyProviderPriority() {
        when(providerRoutingService.updateRouting(eq(RoutingStrategy.BALANCED), eq(List.of()), any())).thenReturn(createMockRoutingUpdate());

        var result = providerRoutingMcpTools.updateProviderRouting("BALANCED", "", null);

        assertThat(result).isNotNull();
        verify(providerRoutingService).updateRouting(eq(RoutingStrategy.BALANCED), eq(List.of()), any());
    }

    @Test
    void shouldParseConditionsCorrectly() {
        when(providerRoutingService.updateRouting(any(), any(), any())).thenReturn(createMockRoutingUpdate());

        var result = providerRoutingMcpTools.updateProviderRouting("PERFORMANCE_OPTIMIZED", null, "max_cost=0.02,min_success_rate=0.98,region=europe");

        assertThat(result).isNotNull();
        verify(providerRoutingService).updateRouting(eq(RoutingStrategy.PERFORMANCE_OPTIMIZED), any(), argThat(conditions ->
            conditions.containsKey("max_cost") &&
                conditions.get("max_cost").equals("0.02") &&
                conditions.containsKey("min_success_rate") &&
                conditions.get("min_success_rate").equals("0.98") &&
                conditions.containsKey("region") &&
                conditions.get("region").equals("europe")
        ));
    }

    private QueryMetricsService queryMetricsService() {
        return (timeWindow, provider) -> new QueryMetricsSnapshot(
            provider == null ? 2 : 1,
            new BigDecimal("0.02"),
            new BigDecimal("0.0100"),
            0.95,
            120,
            new QueryMetricsSnapshot.Trends("+0%", "+0%", "+0%"),
            List.of(new QueryMetricsSnapshot.ProviderMetric(provider == null ? "nominatim" : provider, 1, new BigDecimal("0.01"), 0.95, 120))
        );
    }

    private RoutingUpdate createMockRoutingUpdate() {
        return new RoutingUpdate(true, "Routing updated successfully", "BALANCED", "COST_OPTIMIZED", 25, 150L, true);
    }

    private ProviderHealth createMockProviderHealth(String provider, String status) {
        return new ProviderHealth(provider, status, 800L, 0.05, null, List.of("Provider performing well"));
    }
}
