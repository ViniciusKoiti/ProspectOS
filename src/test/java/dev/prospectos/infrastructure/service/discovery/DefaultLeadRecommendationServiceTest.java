package dev.prospectos.infrastructure.service.discovery;

import java.math.BigDecimal;
import java.util.List;

import dev.prospectos.api.dto.LeadRecommendationRequest;
import dev.prospectos.api.mcp.QueryMetricsService;
import dev.prospectos.api.mcp.QueryMetricsSnapshot;
import dev.prospectos.api.mcp.QueryTimeWindow;
import dev.prospectos.infrastructure.service.compliance.AllowedSourcesComplianceService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultLeadRecommendationServiceTest {

    @Test
    void recommendPrefersObservedProviderWithBestSuccessAndLatency() {
        var metricsService = mock(QueryMetricsService.class);
        var complianceService = mock(AllowedSourcesComplianceService.class);
        when(complianceService.recommendationSources(null)).thenReturn(List.of("google-places", "amazon-location", "scraper"));
        when(metricsService.getMetrics(QueryTimeWindow.TWENTY_FOUR_HOURS, null)).thenReturn(new QueryMetricsSnapshot(
            180,
            new BigDecimal("12.30"),
            new BigDecimal("0.07"),
            0.91,
            420,
            new QueryMetricsSnapshot.Trends("stable", "stable", "up"),
            List.of(
                new QueryMetricsSnapshot.ProviderMetric("google-places", 80, new BigDecimal("6.40"), 0.97, 220),
                new QueryMetricsSnapshot.ProviderMetric("amazon-location", 60, new BigDecimal("4.20"), 0.91, 310),
                new QueryMetricsSnapshot.ProviderMetric("scraper", 40, new BigDecimal("1.70"), 0.82, 900)
            )
        ));

        var service = new DefaultLeadRecommendationService(metricsService, complianceService);
        var response = service.recommend(new LeadRecommendationRequest("dentists in orlando", 20, null, null, null));

        assertThat(response.recommendedSource()).isEqualTo("google-places");
        assertThat(response.fallbackSources()).containsExactly("amazon-location", "scraper");
        assertThat(response.expectedLatencyMs()).isEqualTo(220);
        assertThat(response.reason()).contains("97%");
    }

    @Test
    void recommendUsesAllAllowedSourcesWhenRequestDoesNotPinSources() {
        var metricsService = mock(QueryMetricsService.class);
        var complianceService = mock(AllowedSourcesComplianceService.class);
        when(complianceService.recommendationSources(null)).thenReturn(List.of("in-memory", "google-places"));
        when(metricsService.getMetrics(QueryTimeWindow.TWENTY_FOUR_HOURS, null)).thenReturn(new QueryMetricsSnapshot(
            0,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            0.0d,
            0L,
            new QueryMetricsSnapshot.Trends("stable", "stable", "stable"),
            List.of()
        ));

        var service = new DefaultLeadRecommendationService(metricsService, complianceService);
        var response = service.recommend(new LeadRecommendationRequest("software companies", 10, null, null, null));

        assertThat(response.recommendedSource()).isEqualTo("google-places");
        assertThat(response.fallbackSources()).containsExactly("in-memory");
    }

    @Test
    void recommendFallsBackToPreferredConfiguredSourceWhenNoMetricsExist() {
        var metricsService = mock(QueryMetricsService.class);
        var complianceService = mock(AllowedSourcesComplianceService.class);
        when(complianceService.recommendationSources(List.of("vector-company", "google-places"))).thenReturn(List.of("vector-company", "google-places"));
        when(metricsService.getMetrics(QueryTimeWindow.TWENTY_FOUR_HOURS, null)).thenReturn(new QueryMetricsSnapshot(
            0,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            0.0d,
            0L,
            new QueryMetricsSnapshot.Trends("stable", "stable", "stable"),
            List.of()
        ));

        var service = new DefaultLeadRecommendationService(metricsService, complianceService);
        var response = service.recommend(new LeadRecommendationRequest("software companies", 10, List.of("vector-company", "google-places"), null, null));

        assertThat(response.recommendedSource()).isEqualTo("google-places");
        assertThat(response.fallbackSources()).containsExactly("vector-company");
        assertThat(response.reason()).contains("no observed history yet");
    }
}
