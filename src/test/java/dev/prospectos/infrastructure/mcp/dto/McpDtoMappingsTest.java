package dev.prospectos.infrastructure.mcp.dto;

import dev.prospectos.api.mcp.InternationalSearchOutcome;
import dev.prospectos.api.mcp.ProviderHealth;
import dev.prospectos.api.mcp.QueryMetricsSnapshot;
import dev.prospectos.api.mcp.RoutingStrategy;
import dev.prospectos.api.mcp.RoutingUpdate;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class McpDtoMappingsTest {

    @Test
    void mapsQueryMetricsSnapshotToResponse() {
        var trends = new QueryMetricsSnapshot.Trends("+5%", "+1%", "+7%");
        var breakdown = List.of(
            new QueryMetricsSnapshot.ProviderMetric("google-places", 12, new BigDecimal("3.60"), 0.95, 800)
        );
        var snapshot = new QueryMetricsSnapshot(
            12,
            new BigDecimal("3.60"),
            new BigDecimal("0.3000"),
            0.95,
            800,
            trends,
            breakdown
        );

        var response = QueryMetricsResponse.fromDomain(snapshot);

        assertThat(response.totalQueries()).isEqualTo(12);
        assertThat(response.totalCost()).hasToString("3.60");
        assertThat(response.avgCostPerQuery()).hasToString("0.3000");
        assertThat(response.successRate()).isEqualTo(0.95);
        assertThat(response.avgResponseTime()).isEqualTo(800L);
        assertThat(response.trends().costTrend()).isEqualTo("+5%");
        assertThat(response.providerBreakdown()).containsKey("google-places");
    }

    @Test
    void mapsRoutingAndHealthDomainObjects() {
        var providerHealth = new ProviderHealth(
            "bing-maps",
            "degraded",
            1400,
            0.08,
            "rate limited",
            List.of("Reduce traffic during business hours.")
        );
        var routingUpdate = new RoutingUpdate(
            true,
            RoutingStrategy.BALANCED,
            List.of("google-places", "bing-maps", "nominatim"),
            "Balanced routing applied.",
            Instant.parse("2026-03-29T12:00:00Z")
        );

        var healthStatus = ProviderHealthStatus.fromDomain(providerHealth);
        var updateResult = RoutingUpdateResult.fromDomain(routingUpdate);

        assertThat(healthStatus.provider()).isEqualTo("bing-maps");
        assertThat(healthStatus.status()).isEqualTo("degraded");
        assertThat(healthStatus.lastError()).isEqualTo("rate limited");
        assertThat(updateResult.configurationApplied()).isTrue();
        assertThat(updateResult.strategy()).isEqualTo("BALANCED");
        assertThat(updateResult.providerPriority()).containsExactly("google-places", "bing-maps", "nominatim");
        assertThat(updateResult.updatedAt()).isEqualTo(Instant.parse("2026-03-29T12:00:00Z"));
    }

    @Test
    void mapsInternationalSearchOutcome() {
        var outcome = new InternationalSearchOutcome(
            List.of(
                new InternationalSearchOutcome.LeadCandidate("Dallas Dental", "US", "bing-maps", 0.91)
            ),
            new BigDecimal("12.40"),
            0.91,
            List.of("Prioritize Bing for suburban US queries.")
        );

        var result = InternationalSearchResult.fromDomain(outcome);

        assertThat(result.totalCost()).hasToString("12.40");
        assertThat(result.avgQualityScore()).isEqualTo(0.91);
        assertThat(result.optimizationHints()).containsExactly("Prioritize Bing for suburban US queries.");
        assertThat(result.leads()).singleElement().satisfies(lead -> {
            assertThat(lead.name()).isEqualTo("Dallas Dental");
            assertThat(lead.country()).isEqualTo("US");
            assertThat(lead.sourceProvider()).isEqualTo("bing-maps");
            assertThat(lead.qualityScore()).isEqualTo(0.91);
        });
    }
}
