package dev.prospectos.integration;

import dev.prospectos.ai.config.VectorizationProperties;
import dev.prospectos.ai.vector.VectorIndex;
import dev.prospectos.ai.vector.VectorSearchMatch;
import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.infrastructure.service.discovery.DiscoveredLeadCandidate;
import dev.prospectos.infrastructure.service.discovery.DiscoveryContext;
import dev.prospectos.infrastructure.service.discovery.SemanticCompanyMatch;
import dev.prospectos.infrastructure.service.discovery.SemanticCompanySearchService;
import dev.prospectos.infrastructure.service.discovery.VectorCompanyLeadDiscoverySource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = {
    SemanticCompanySearchService.class,
    VectorCompanyLeadDiscoverySource.class,
    SemanticCompanySearchIntegrationTest.SemanticSearchTestConfiguration.class
})
class SemanticCompanySearchIntegrationTest {

    @Autowired
    private SemanticCompanySearchService semanticCompanySearchService;

    @Autowired
    private VectorCompanyLeadDiscoverySource vectorCompanyLeadDiscoverySource;

    @MockitoBean
    private CompanyDataService companyDataService;

    @MockitoBean
    private VectorIndex vectorIndex;

    @Test
    void searchReturnsEmptyForBlankQueryOrNonPositiveTopK() {
        assertThat(semanticCompanySearchService.search(" ", 5)).isEmpty();
        assertThat(semanticCompanySearchService.search("software", 0)).isEmpty();

        verify(vectorIndex, never()).similaritySearch(eq("software"), eq(0), anyDouble());
    }

    @Test
    void searchResolvesCompanyIdsFromMetadataAndFallbackId() {
        CompanyDTO numericCompany = company(10L, "Numeric Co");
        CompanyDTO stringCompany = company(11L, "String Co");
        CompanyDTO fallbackCompany = company(12L, "Fallback Co");

        given(vectorIndex.similaritySearch("growth", 3, 0.2d)).willReturn(List.of(
            new VectorSearchMatch("ignored", 0.91d, Map.of("companyId", 10L)),
            new VectorSearchMatch("ignored-2", 0.82d, Map.of("companyId", "11")),
            new VectorSearchMatch("company:12", 0.73d, Map.of("other", "value"))
        ));
        given(companyDataService.findCompany(10L)).willReturn(numericCompany);
        given(companyDataService.findCompany(11L)).willReturn(stringCompany);
        given(companyDataService.findCompany(12L)).willReturn(fallbackCompany);

        List<SemanticCompanyMatch> matches = semanticCompanySearchService.search("growth", 3);

        assertThat(matches)
            .extracting(match -> match.company().name())
            .containsExactly("Numeric Co", "String Co", "Fallback Co");
    }

    @Test
    void searchFiltersInvalidMetadataAndMissingCompanies() {
        given(vectorIndex.similaritySearch("ops", 4, 0.2d)).willReturn(List.of(
            new VectorSearchMatch("invalid-text", 0.91d, Map.of("companyId", "abc")),
            new VectorSearchMatch("company:not-a-number", 0.84d, Map.of()),
            new VectorSearchMatch("company:50", 0.80d, Map.of("other", "value"))
        ));
        given(companyDataService.findCompany(50L)).willReturn(null);

        List<SemanticCompanyMatch> matches = semanticCompanySearchService.search("ops", 4);

        assertThat(matches).isEmpty();
    }

    @Test
    void vectorDiscoverySourceMapsSemanticMatchesToCandidates() {
        CompanyDTO company = company(21L, "Discovery Co");
        given(vectorIndex.similaritySearch("cloud", 2, 0.2d)).willReturn(List.of(
            new VectorSearchMatch("company:21", 0.88d, Map.of("companyId", 21L))
        ));
        given(companyDataService.findCompany(21L)).willReturn(company);

        List<DiscoveredLeadCandidate> leads = vectorCompanyLeadDiscoverySource.discover(
            new DiscoveryContext("cloud", "CTO", 2, null)
        );

        assertThat(leads).singleElement().satisfies(lead -> {
            assertThat(lead.name()).isEqualTo("Discovery Co");
            assertThat(lead.website()).isEqualTo("https://discovery.example.com");
            assertThat(lead.sourceName()).isEqualTo("vector-company");
        });
    }

    private CompanyDTO company(Long id, String name) {
        return new CompanyDTO(
            id,
            name,
            "Technology",
            "https://discovery.example.com",
            "Cloud engineering partner",
            80,
            "Sao Paulo, BR",
            null
        );
    }

    @TestConfiguration
    static class SemanticSearchTestConfiguration {

        @Bean
        VectorizationProperties vectorizationProperties() {
            return new VectorizationProperties("in-memory", "hashing-v1", 128, 5, 0.2d, null);
        }
    }
}
