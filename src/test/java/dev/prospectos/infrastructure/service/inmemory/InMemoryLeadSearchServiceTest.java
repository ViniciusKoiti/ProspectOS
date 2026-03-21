package dev.prospectos.infrastructure.service.inmemory;

import java.util.List;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.dto.LeadResultDTO;
import dev.prospectos.api.dto.LeadSearchRequest;
import dev.prospectos.api.dto.LeadSearchResponse;
import dev.prospectos.api.dto.LeadSearchStatus;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.infrastructure.config.LeadSearchProperties;
import dev.prospectos.infrastructure.service.compliance.AllowedSourcesComplianceService;
import dev.prospectos.infrastructure.service.scoring.CompanyScoringService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InMemoryLeadSearchServiceTest {

    @Mock
    private CompanyDataService companyDataService;

    @Mock
    private ICPDataService icpDataService;

    @Mock
    private CompanyScoringService scoringService;

    @Mock
    private AllowedSourcesComplianceService complianceService;

    @Test
    void searchLeadsUsesDefaultIcpAndBuildsLeadResults() {
        InMemoryLeadSearchService service = new InMemoryLeadSearchService(
            companyDataService,
            icpDataService,
            scoringService,
            complianceService,
            new LeadSearchProperties(1L)
        );

        when(companyDataService.findAllCompanies()).thenReturn(List.of(
            new CompanyDTO(1L, "SoftNow", "software", "https://softnow.com", "software platform", 20, "Sao Paulo", null),
            new CompanyDTO(2L, "CodeWave", "software", "https://codewave.com", "software studio", 15, "Rio", null),
            new CompanyDTO(3L, "CloudFast", "software", "https://cloudfast.com", "cloud software", 10, "Curitiba", null)
        ));
        when(complianceService.validateSources(List.of("in-memory"))).thenReturn(List.of("in-memory"));
        when(icpDataService.findICP(1L)).thenReturn(ICPDto.createMock());
        when(scoringService.scoreCandidate(any(Company.class), any(ICP.class))).thenReturn(new ScoreDTO(85, "HOT", "Good fit"));

        LeadSearchResponse response = service.searchLeads(new LeadSearchRequest("software", 2, List.of("in-memory"), null));

        assertEquals(LeadSearchStatus.COMPLETED, response.status());
        assertEquals(2, response.leads().size());
        for (LeadResultDTO lead : response.leads()) {
            assertEquals("in-memory", lead.source().sourceName());
            assertEquals(85, lead.score().value());
            assertFalse(lead.leadKey().isBlank());
        }
        verify(icpDataService).findICP(1L);
    }

    @Test
    void searchLeadsThrowsWhenValidatedSourcesAreBlank() {
        InMemoryLeadSearchService service = new InMemoryLeadSearchService(
            companyDataService,
            icpDataService,
            scoringService,
            complianceService,
            new LeadSearchProperties(1L)
        );

        when(complianceService.validateSources(null)).thenReturn(Arrays.asList(" ", null));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.searchLeads(new LeadSearchRequest("curitiba", 10, null, 1L))
        );

        assertEquals(
            "No lead sources configured. Configure prospectos.leads.default-sources or provide sources in request",
            exception.getMessage()
        );
    }

    @Test
    void searchLeadsThrowsWhenNoRequestIcpAndNoDefaultConfigured() {
        InMemoryLeadSearchService service = new InMemoryLeadSearchService(
            companyDataService,
            icpDataService,
            scoringService,
            complianceService,
            new LeadSearchProperties(null)
        );

        when(complianceService.validateSources(List.of("in-memory"))).thenReturn(List.of("in-memory"));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.searchLeads(new LeadSearchRequest("software", 1, List.of("in-memory"), null))
        );
        assertEquals(
            "ICP ID is required. Provide icpId in request or configure prospectos.leads.default-icp-id",
            exception.getMessage()
        );
    }
}
