package dev.prospectos.infrastructure.service.leads;

import dev.prospectos.ai.client.ScraperClientInterface;
import dev.prospectos.ai.client.ScrapingResponse;
import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.LeadDiscoveryService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.LeadResultDTO;
import dev.prospectos.api.dto.LeadSearchResponse;
import dev.prospectos.api.dto.LeadSearchStatus;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.mcp.QueryMetricsRecorder;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.enrichment.CompanyEnrichmentService;
import dev.prospectos.infrastructure.service.inmemory.InMemoryLeadResultFactory;
import dev.prospectos.infrastructure.service.scoring.CompanyScoringService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScraperLeadSourceDispatcherTest {

    @Mock
    private CompanyDataService companyDataService;
    @Mock
    private LeadDiscoveryService leadDiscoveryService;
    @Mock
    private CompanyScoringService scoringService;
    @Mock
    private QueryMetricsRecorder metricsRecorder;
    @Mock
    private ScraperClientInterface scraperClient;
    @Mock
    private CompanyEnrichmentService enrichmentService;

    @Test
    void recordsInMemoryExecution() {
        var dispatcher = newDispatcher(Set.of("amazon-location"));
        var icp = ICP.create("ICP", "desc", List.of("Software"), List.of("BR"), List.of("CTO"), "cloud");
        when(companyDataService.findAllCompanies()).thenReturn(List.of(CompanyDTO.createMock()));
        when(scoringService.scoreCandidate(org.mockito.ArgumentMatchers.any(), eq(icp)))
            .thenReturn(new ScoreDTO(82, "HOT", "Great fit"));

        var results = dispatcher.searchBySource("in-memory", new ScraperLeadRequestContext(5, List.of("in-memory"), "TechCorp", "TechCorp"), 1L, icp);

        assertThat(results).hasSize(1);
        verify(metricsRecorder).recordExecution(eq("in-memory"), anyLong(), eq(true), eq(1));
    }

    @Test
    void doesNotDoubleRecordDiscoverySources() {
        var dispatcher = newDispatcher(Set.of("amazon-location"));
        var response = new LeadSearchResponse(LeadSearchStatus.COMPLETED, List.of(mockLead()), java.util.UUID.randomUUID(), "ok");
        when(leadDiscoveryService.discoverLeads(org.mockito.ArgumentMatchers.any())).thenReturn(response);

        var results = dispatcher.searchBySource("amazon-location", new ScraperLeadRequestContext(5, List.of("amazon-location"), "clinicas", "clinicas"), 1L, null);

        assertThat(results).hasSize(1);
        verify(metricsRecorder, never()).recordExecution(eq("amazon-location"), anyLong(), eq(true), eq(1));
    }

    @Test
    void recordsWebsiteFailuresForNonDiscoverySources() {
        var dispatcher = newDispatcher(Set.of("amazon-location"));
        when(scraperClient.scrapeWebsiteSync("https://acme.com", false)).thenReturn(new ScrapingResponse(false, null, "Scraper failed"));

        assertThatThrownBy(() -> dispatcher.searchBySource(
            "scraper",
            new ScraperLeadRequestContext(5, List.of("scraper"), "https://acme.com", "https://acme.com"),
            1L,
            null
        )).isInstanceOf(IllegalStateException.class).hasMessage("Scraper failed");

        verify(metricsRecorder).recordExecution(eq("scraper"), anyLong(), eq(false), eq(0));
    }

    private ScraperLeadSourceDispatcher newDispatcher(Set<String> discoverySources) {
        var websiteLeadSearch = new ScraperWebsiteLeadSearch(
            scraperClient,
            enrichmentService,
            scoringService,
            new ScraperLeadCandidateFactory()
        );
        return new ScraperLeadSourceDispatcher(
            companyDataService,
            leadDiscoveryService,
            new InMemoryLeadResultFactory(scoringService),
            websiteLeadSearch,
            discoverySources,
            metricsRecorder
        );
    }

    private LeadResultDTO mockLead() {
        return new InMemoryLeadResultFactory(scoringService).toLeadResult(
            CompanyDTO.createMock(),
            "amazon-location",
            ICP.create("ICP", "desc", List.of("Software"), List.of("BR"), List.of("CTO"), "cloud")
        );
    }
}