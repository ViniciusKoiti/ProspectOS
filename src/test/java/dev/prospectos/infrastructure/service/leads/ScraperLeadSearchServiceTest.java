package dev.prospectos.infrastructure.service.leads;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.prospectos.ai.client.ScraperClientInterface;
import dev.prospectos.ai.client.ScrapingResponse;
import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.dto.LeadResultDTO;
import dev.prospectos.api.dto.LeadSearchRequest;
import dev.prospectos.api.dto.LeadSearchResponse;
import dev.prospectos.api.dto.LeadSearchStatus;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.CompanySize;
import dev.prospectos.core.domain.Email;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.domain.Website;
import dev.prospectos.core.enrichment.CompanyEnrichmentService;
import dev.prospectos.core.enrichment.EnrichmentQuality;
import dev.prospectos.core.enrichment.EnrichmentResult;
import dev.prospectos.core.enrichment.ValidatedContact;
import dev.prospectos.infrastructure.config.LeadSearchProperties;
import dev.prospectos.infrastructure.service.compliance.AllowedSourcesComplianceService;
import dev.prospectos.infrastructure.service.scoring.CompanyScoringService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScraperLeadSearchServiceTest {

    @Mock
    private ScraperClientInterface scraperClient;

    @Mock
    private CompanyEnrichmentService enrichmentService;

    @Mock
    private ICPDataService icpDataService;

    @Mock
    private CompanyScoringService scoringService;

    @Mock
    private AllowedSourcesComplianceService complianceService;

    private ScraperLeadSearchService service;

    @BeforeEach
    void setUp() {
        service = new ScraperLeadSearchService(
            scraperClient,
            enrichmentService,
            icpDataService,
            scoringService,
            complianceService,
            new LeadSearchProperties(1L)
        );
    }

    @Test
    void searchLeads_ReturnsFailedWhenScraperFails() {
        when(complianceService.validateSources(List.of("scraper"))).thenReturn(List.of("scraper"));
        when(scraperClient.scrapeWebsiteSync("https://acme.com", false))
            .thenReturn(new ScrapingResponse(false, null, "timeout"));

        LeadSearchResponse response = service.searchLeads(
            new LeadSearchRequest("acme.com", 1, List.of("scraper"), null)
        );

        assertEquals(LeadSearchStatus.FAILED, response.status());
        assertTrue(response.leads().isEmpty());
        assertTrue(response.message().contains("timeout"));
    }

    @Test
    void searchLeads_UsesDefaultIcpAndReturnsLeadWhenScraperSucceeds() {
        when(complianceService.validateSources(List.of("scraper"))).thenReturn(List.of("scraper"));
        when(scraperClient.scrapeWebsiteSync("https://acme.com", false))
            .thenReturn(new ScrapingResponse(true, Map.of("company_name", "Acme"), null));
        when(enrichmentService.enrichCompanyData(any())).thenReturn(successfulEnrichment());
        when(icpDataService.findICP(1L)).thenReturn(ICPDto.createMock());
        when(scoringService.scoreCandidate(any(Company.class), any(ICP.class))).thenReturn(new ScoreDTO(80, "HOT", "fit"));

        LeadSearchResponse response = service.searchLeads(
            new LeadSearchRequest("acme.com", 1, List.of("scraper"), null)
        );

        assertEquals(LeadSearchStatus.COMPLETED, response.status());
        assertEquals(1, response.leads().size());
        LeadResultDTO lead = response.leads().getFirst();
        assertEquals("Acme", lead.candidate().name());
        assertEquals("https://acme.com", lead.source().sourceUrl());
        assertEquals("scraper", lead.source().sourceName());
        assertEquals(80, lead.score().value());
        verify(scraperClient).scrapeWebsiteSync("https://acme.com", false);
        verify(icpDataService).findICP(1L);
    }

    @Test
    void searchLeads_ReturnsNoLeadsWhenCandidateWebsiteIsInvalid() {
        when(complianceService.validateSources(List.of("scraper"))).thenReturn(List.of("scraper"));
        when(scraperClient.scrapeWebsiteSync("acme company", false))
            .thenReturn(new ScrapingResponse(true, Map.of("company_name", "Acme"), null));
        when(enrichmentService.enrichCompanyData(any())).thenReturn(enrichmentWithoutWebsite());

        LeadSearchResponse response = service.searchLeads(
            new LeadSearchRequest("acme company", 1, List.of("scraper"), null)
        );

        assertEquals(LeadSearchStatus.COMPLETED, response.status());
        assertTrue(response.leads().isEmpty());
        assertEquals("No leads found", response.message());
        verify(icpDataService, never()).findICP(any());
    }

    @Test
    void searchLeads_ThrowsWhenNoIcpProvidedAndNoDefaultConfigured() {
        ScraperLeadSearchService localService = new ScraperLeadSearchService(
            scraperClient,
            enrichmentService,
            icpDataService,
            scoringService,
            complianceService,
            new LeadSearchProperties(null)
        );

        when(complianceService.validateSources(List.of("scraper"))).thenReturn(List.of("scraper"));
        when(scraperClient.scrapeWebsiteSync("https://acme.com", false))
            .thenReturn(new ScrapingResponse(true, Map.of("company_name", "Acme"), null));
        when(enrichmentService.enrichCompanyData(any())).thenReturn(successfulEnrichment());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> localService.searchLeads(new LeadSearchRequest("acme.com", 1, List.of("scraper"), null))
        );

        assertEquals(
            "ICP ID is required. Provide icpId in request or configure prospectos.leads.default-icp-id",
            exception.getMessage()
        );
    }

    private EnrichmentResult successfulEnrichment() {
        return new EnrichmentResult(
            "Acme",
            "Desc",
            List.of(ValidatedContact.valid(Email.of("sales@acme.com"), ValidatedContact.ContactType.CORPORATE)),
            "123",
            List.of("Java"),
            "Technology",
            CompanySize.SMALL,
            List.of("News"),
            Website.of("https://acme.com"),
            EnrichmentQuality.calculate(1, 1, 1, 0, 0, 0, 6, 6)
        );
    }

    private EnrichmentResult enrichmentWithoutWebsite() {
        return new EnrichmentResult(
            "Acme",
            "Desc",
            List.of(),
            "123",
            List.of(),
            "Technology",
            null,
            List.of(),
            null,
            EnrichmentQuality.calculate(0, 0, 0, 0, 0, 0, 2, 6)
        );
    }
}

