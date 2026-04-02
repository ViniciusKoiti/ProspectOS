package dev.prospectos.infrastructure.mcp.tools;

import dev.prospectos.api.mcp.CompanyIntelligence;
import dev.prospectos.api.mcp.CompetitorAnalysis;
import dev.prospectos.api.mcp.EnrichedLeadData;
import dev.prospectos.api.mcp.InternationalSearchResult;
import dev.prospectos.api.mcp.InternationalSearchService;
import dev.prospectos.api.mcp.LeadData;
import dev.prospectos.api.mcp.LeadSearchCriteria;
import dev.prospectos.api.mcp.MarketCoverageAnalysis;
import dev.prospectos.api.mcp.SearchQualityMetrics;
import dev.prospectos.api.mcp.SearchStrategy;
import dev.prospectos.api.mcp.TechnologyStack;
import dev.prospectos.api.mcp.ContactData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class InternationalSearchMcpToolsTest {

    @Mock
    private InternationalSearchService internationalSearchService;

    private InternationalSearchMcpTools internationalSearchMcpTools;

    @BeforeEach
    void setUp() {
        internationalSearchMcpTools = new InternationalSearchMcpTools(internationalSearchService, new InternationalSearchMcpInputParser());
    }

    @Test
    void shouldSearchInternationalLeadsWithValidParameters() {
        var mockResult = createMockSearchResult();
        when(internationalSearchService.searchLeads(eq("brazil"), eq("technology"), any())).thenReturn(mockResult);

        var result = internationalSearchMcpTools.searchInternationalLeads("brazil", "technology", "20", "100.0", "0.8", "website,email");

        assertThat(result).isNotNull();
        assertThat(result.country()).isEqualTo("brazil");
        assertThat(result.industry()).isEqualTo("technology");
        assertThat(result.leads()).hasSize(2);
        assertThat(result.totalCost()).isEqualTo(15.50);
        assertThat(result.quality().overallScore()).isEqualTo(0.85);
        verify(internationalSearchService).searchLeads(eq("brazil"), eq("technology"), any(LeadSearchCriteria.class));
    }

    @Test
    void shouldUseDefaultParametersWhenNotProvided() {
        when(internationalSearchService.searchLeads(eq("brazil"), eq("technology"), any())).thenReturn(createMockSearchResult());

        var result = internationalSearchMcpTools.searchInternationalLeads("brazil", "technology", null, null, null, null);

        assertThat(result).isNotNull();
        verify(internationalSearchService).searchLeads(eq("brazil"), eq("technology"), argThat(criteria ->
            criteria.maxResults() == 20 &&
                criteria.budgetLimit() == 50.0 &&
                criteria.minQualityScore() == 0.7 &&
                criteria.requiredFields().contains("companyName") &&
                criteria.requiredFields().contains("website")
        ));
    }

    @Test
    void shouldValidateCountryParameter() {
        assertThatThrownBy(() -> internationalSearchMcpTools.searchInternationalLeads("", "technology", null, null, null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Country parameter is required");
    }

    @Test
    void shouldValidateIndustryParameter() {
        assertThatThrownBy(() -> internationalSearchMcpTools.searchInternationalLeads("brazil", "", null, null, null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Industry parameter is required");
    }

    @Test
    void shouldValidateMaxResultsRange() {
        assertThatThrownBy(() -> internationalSearchMcpTools.searchInternationalLeads("brazil", "technology", "150", null, null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("maxResults must be between 1 and 100");
    }

    @Test
    void shouldValidateQualityScoreRange() {
        assertThatThrownBy(() -> internationalSearchMcpTools.searchInternationalLeads("brazil", "technology", null, null, "1.5", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("minQualityScore must be between 0.0 and 1.0");
    }

    @Test
    void shouldEnrichInternationalLead() {
        when(internationalSearchService.enrichLead(any(), any())).thenReturn(createMockEnrichedLeadData());

        var result = internationalSearchMcpTools.enrichInternationalLead("lead123", "Tech Company Brazil", "https://techcompany.com.br", "linkedin,crunchbase");

        assertThat(result).isNotNull();
        assertThat(result.basicData().companyName()).isEqualTo("Tech Company Brazil");
        assertThat(result.enrichmentScore()).isEqualTo(0.92);
        assertThat(result.contacts()).hasSize(2);
        assertThat(result.intelligence().estimatedEmployees()).isEqualTo(150);
        verify(internationalSearchService).enrichLead(any(LeadData.class), eq(List.of("linkedin", "crunchbase")));
    }

    @Test
    void shouldUseDefaultSourcesForEnrichment() {
        when(internationalSearchService.enrichLead(any(), any())).thenReturn(createMockEnrichedLeadData());

        var result = internationalSearchMcpTools.enrichInternationalLead("lead123", "Tech Company Brazil", null, null);

        assertThat(result).isNotNull();
        verify(internationalSearchService).enrichLead(any(LeadData.class), eq(List.of("linkedin", "web-scraping", "google-places")));
    }

    @Test
    void shouldOptimizeSearchStrategy() {
        when(internationalSearchService.optimizeStrategy("latin-america", 500.0, 0.9)).thenReturn(createMockSearchStrategy());

        var result = internationalSearchMcpTools.optimizeSearchStrategy("latin-america", "500.0", "0.9");

        assertThat(result).isNotNull();
        assertThat(result.recommendedSources()).hasSize(3);
        assertThat(result.estimatedCost()).isEqualTo(450.0);
        assertThat(result.estimatedQuality()).isEqualTo(0.95);
        assertThat(result.optimizationTips()).hasSize(2);
        verify(internationalSearchService).optimizeStrategy("latin-america", 500.0, 0.9);
    }

    @Test
    void shouldAnalyzeMarketCoverage() {
        when(internationalSearchService.analyzeMarketCoverage("brazil", List.of("Competitor A", "Competitor B"))).thenReturn(createMockMarketCoverageAnalysis());

        var result = internationalSearchMcpTools.analyzeMarketCoverage("brazil", "Competitor A,Competitor B");

        assertThat(result).isNotNull();
        assertThat(result.country()).isEqualTo("brazil");
        assertThat(result.totalMarketSize()).isEqualTo(50000000.0);
        assertThat(result.coveredMarketShare()).isEqualTo(0.25);
        assertThat(result.competitorAnalysis()).hasSize(2);
        assertThat(result.opportunities()).hasSize(3);
        verify(internationalSearchService).analyzeMarketCoverage("brazil", List.of("Competitor A", "Competitor B"));
    }

    @Test
    void shouldHandleEmptyCompetitorsList() {
        when(internationalSearchService.analyzeMarketCoverage("brazil", List.of())).thenReturn(createMockMarketCoverageAnalysis());

        var result = internationalSearchMcpTools.analyzeMarketCoverage("brazil", "");

        assertThat(result).isNotNull();
        verify(internationalSearchService).analyzeMarketCoverage("brazil", List.of());
    }

    @Test
    void shouldValidateBudgetParameter() {
        assertThatThrownBy(() -> internationalSearchMcpTools.optimizeSearchStrategy("brazil", "invalid", "0.8"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid budget");
    }

    @Test
    void shouldValidateQualityThresholdParameter() {
        assertThatThrownBy(() -> internationalSearchMcpTools.optimizeSearchStrategy("brazil", "100.0", "invalid"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid qualityThreshold");
    }

    private InternationalSearchResult createMockSearchResult() {
        var leads = List.of(
            new LeadData("lead1", "Tech Company A", "https://techA.com", "technology", "brazil", "Sao Paulo", Map.of(), 0.85),
            new LeadData("lead2", "Tech Company B", "https://techB.com", "technology", "brazil", "Rio de Janeiro", Map.of(), 0.90)
        );
        return new InternationalSearchResult("brazil", "technology", leads, new SearchQualityMetrics(0.85, 0.90, 0.88, 0.82, 1, List.of("Minor quality issues")), 15.50, List.of("linkedin", "google-places", "web-scraping"));
    }

    private EnrichedLeadData createMockEnrichedLeadData() {
        var basicData = new LeadData("lead123", "Tech Company Brazil", "https://techcompany.com.br", "technology", "brazil", "Sao Paulo", Map.of(), 0.8);
        var intelligence = new CompanyIntelligence(150, "$10M", "Series B", List.of("CEO Joao Silva", "CTO Maria Santos"), Map.of("linkedinFollowers", 5000));
        var contacts = List.of(
            new ContactData("Joao Silva", "CEO", "joao@techcompany.com.br", "linkedin.com/in/joaosilva", 0.9),
            new ContactData("Maria Santos", "CTO", "maria@techcompany.com.br", "linkedin.com/in/mariasantos", 0.85)
        );
        return new EnrichedLeadData(basicData, intelligence, contacts, new TechnologyStack(List.of("React", "Spring Boot"), List.of("AWS"), List.of("Docker"), "AWS"), 0.92);
    }

    private SearchStrategy createMockSearchStrategy() {
        return new SearchStrategy(List.of("linkedin", "crunchbase", "web-scraping"), Map.of("linkedin", 0.5, "crunchbase", 0.3, "web-scraping", 0.2), 450.0, 0.95, List.of("Focus on high-value prospects", "Use local language processing"));
    }

    private MarketCoverageAnalysis createMockMarketCoverageAnalysis() {
        return new MarketCoverageAnalysis(
            "brazil",
            50000000.0,
            0.25,
            List.of("Startups", "Family businesses"),
            Map.of(
                "Competitor A", new CompetitorAnalysis("Competitor A", 0.15, List.of("Enterprise"), List.of("Strong brand"), List.of("High pricing")),
                "Competitor B", new CompetitorAnalysis("Competitor B", 0.10, List.of("SMB"), List.of("Local expertise"), List.of("Limited tech"))
            ),
            List.of("Target small businesses", "Focus on rural markets", "Leverage digital channels")
        );
    }
}
