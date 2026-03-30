package dev.prospectos.infrastructure.mcp.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import dev.prospectos.api.mcp.LeadData;
import dev.prospectos.api.mcp.LeadSearchCriteria;

class DefaultInternationalSearchServiceTest {

    private final DefaultInternationalSearchService service = new DefaultInternationalSearchService();

    @Test
    void shouldSearchInternationalLeadsWithinCriteria() {
        var criteria = new LeadSearchCriteria(5, 25.0, 0.7, List.of("website"), Map.of());

        var result = service.searchLeads("brazil", "technology", criteria);

        assertThat(result.country()).isEqualTo("brazil");
        assertThat(result.industry()).isEqualTo("technology");
        assertThat(result.leads()).hasSizeLessThanOrEqualTo(5);
        assertThat(result.quality().overallScore()).isBetween(0.7, 0.95);
        assertThat(result.totalCost()).isPositive();
        assertThat(result.sourcesUsed()).isNotEmpty();
    }

    @Test
    void shouldEnrichLeadWithContactsAndTechStack() {
        var lead = new LeadData("lead-1", "Acme", "https://www.acme.com", "technology", "brazil", "Sao Paulo", Map.of(), 0.8);

        var enriched = service.enrichLead(lead, List.of("linkedin", "google-places"));

        assertThat(enriched.basicData()).isEqualTo(lead);
        assertThat(enriched.contacts()).hasSize(2);
        assertThat(enriched.techStack().frameworks()).isNotEmpty();
        assertThat(enriched.enrichmentScore()).isBetween(0.6, 0.95);
    }

    @Test
    void shouldOptimizeStrategyAndAnalyzeMarketCoverage() {
        var strategy = service.optimizeStrategy("latin-america", 150.0, 0.9);
        var analysis = service.analyzeMarketCoverage("brazil", List.of("Comp A", "Comp B"));

        assertThat(strategy.recommendedSources()).isNotEmpty();
        assertThat(strategy.sourceWeights()).isNotEmpty();
        assertThat(strategy.optimizationTips()).isNotEmpty();
        assertThat(strategy.estimatedCost()).isPositive();
        assertThat(strategy.estimatedQuality()).isGreaterThan(0.9);

        assertThat(analysis.country()).isEqualTo("brazil");
        assertThat(analysis.uncoveredSegments()).isNotEmpty();
        assertThat(analysis.competitorAnalysis()).hasSize(2);
        assertThat(analysis.opportunities()).isNotEmpty();
    }
}
