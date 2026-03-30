package dev.prospectos.infrastructure.service.discovery;

import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.dto.CompanyCandidateDTO;
import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.dto.LeadDiscoveryRequest;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.mcp.QueryMetricsRecorder;
import dev.prospectos.infrastructure.config.LeadSearchProperties;
import dev.prospectos.infrastructure.service.compliance.AllowedSourcesComplianceService;
import dev.prospectos.infrastructure.service.scoring.CompanyScoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultLeadDiscoveryServiceTest {

    @Mock
    private ICPDataService icpDataService;

    @Mock
    private CompanyScoringService scoringService;

    @Mock
    private AllowedSourcesComplianceService complianceService;

    @Mock
    private QueryMetricsRecorder queryMetricsRecorder;

    private DefaultLeadDiscoveryService service;

    @BeforeEach
    void setUp() {
        service = new DefaultLeadDiscoveryService(
            List.of(),
            icpDataService,
            scoringService,
            complianceService,
            new LeadSearchProperties(1L),
            queryMetricsRecorder
        );
    }

    @Test
    void discoverLeads_ThrowsWhenValidatedSourceHasNoRegisteredStrategy() {
        when(complianceService.validateSources(List.of("llm-discovery"))).thenReturn(List.of("llm-discovery"));
        when(icpDataService.findICP(1L)).thenReturn(ICPDto.createMock());

        LeadDiscoveryRequest request = new LeadDiscoveryRequest(
            "fornecedores de alimentos",
            "SUPPLIER",
            3,
            List.of("llm-discovery"),
            1L
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.discoverLeads(request)
        );
        assertEquals(
            "Configured source without implementation: llm-discovery",
            exception.getMessage()
        );
    }

    @Test
    void discoverLeads_DeduplicatesCandidatesByLeadKeyAndRespectsLimit() {
        LeadDiscoverySource source = new StubLeadDiscoverySource(
            "llm-discovery",
            List.of(
                candidate("Alpha", "https://alpha.com", "Software"),
                candidate("Alpha Duplicate", "https://alpha.com", "Software"),
                candidate("Beta", "https://beta.com", "Software"),
                candidate("Gamma", "https://gamma.com", "Software")
            )
        );
        service = createService(List.of(source), 1L);

        when(complianceService.validateSources(List.of("llm-discovery"))).thenReturn(List.of("llm-discovery"));
        when(icpDataService.findICP(1L)).thenReturn(ICPDto.createMock());
        when(scoringService.scoreCandidate(any(), any())).thenReturn(new ScoreDTO(80, "HOT", "Great fit"));

        LeadDiscoveryRequest request = new LeadDiscoveryRequest(
            "software",
            "CTO",
            2,
            List.of("llm-discovery"),
            1L
        );

        var response = service.discoverLeads(request);

        assertEquals(2, response.leads().size());
        assertEquals(2, response.leads().stream().map(lead -> lead.leadKey()).distinct().count());
        verify(scoringService, times(2)).scoreCandidate(any(), any());
    }

    @Test
    void discoverLeads_PreservesNoWebsiteCandidateAndNormalizesBlankIndustry() {
        LeadDiscoverySource source = new StubLeadDiscoverySource(
            "llm-discovery",
            List.of(
                candidate("Broken", null, "Software"),
                candidate("Valid", "https://valid.com", "   ")
            )
        );
        service = createService(List.of(source), 1L);

        when(complianceService.validateSources(List.of("llm-discovery"))).thenReturn(List.of("llm-discovery"));
        when(icpDataService.findICP(1L)).thenReturn(ICPDto.createMock());
        when(scoringService.scoreCandidate(any(), any())).thenReturn(new ScoreDTO(65, "WARM", "Potential"));

        LeadDiscoveryRequest request = new LeadDiscoveryRequest(
            "valid",
            "CTO",
            5,
            List.of("llm-discovery"),
            1L
        );

        var response = service.discoverLeads(request);

        assertEquals(2, response.leads().size());
        assertEquals("Software", response.leads().get(0).candidate().industry());
        assertEquals(CompanyCandidateDTO.WebsitePresence.NO_WEBSITE, response.leads().get(0).candidate().websitePresence());
        assertEquals("Other", response.leads().get(1).candidate().industry());
        verify(scoringService, times(2)).scoreCandidate(any(), any());
    }

    @Test
    void discoverLeads_UsesDefaultIcpWhenRequestIcpIsNull() {
        LeadDiscoverySource source = new StubLeadDiscoverySource(
            "llm-discovery",
            List.of(candidate("Default ICP", "https://defaulticp.com", "Software"))
        );
        service = createService(List.of(source), 99L);

        when(complianceService.validateSources(List.of("llm-discovery"))).thenReturn(List.of("llm-discovery"));
        when(icpDataService.findICP(99L)).thenReturn(ICPDto.createMock());
        when(scoringService.scoreCandidate(any(), any())).thenReturn(new ScoreDTO(70, "WARM", "Match"));

        LeadDiscoveryRequest request = new LeadDiscoveryRequest(
            "default",
            "CTO",
            1,
            List.of("llm-discovery"),
            null
        );

        service.discoverLeads(request);
        verify(icpDataService).findICP(99L);
    }

    @Test
    void discoverLeads_UsesConfiguredDefaultSourcesWhenRequestSourcesAreMissing() {
        LeadDiscoverySource source = new StubLeadDiscoverySource(
            "vector-company",
            List.of(candidate("Config Source", "https://config-source.com", "Software"))
        );
        service = createService(List.of(source), 1L);

        when(complianceService.validateSources(null)).thenReturn(List.of("vector-company"));
        when(icpDataService.findICP(1L)).thenReturn(ICPDto.createMock());
        when(scoringService.scoreCandidate(any(), any())).thenReturn(new ScoreDTO(72, "WARM", "Config default"));

        LeadDiscoveryRequest request = new LeadDiscoveryRequest(
            "config",
            "CTO",
            1,
            null,
            1L
        );

        service.discoverLeads(request);

        verify(complianceService).validateSources(null);
    }

    @Test
    void discoverLeads_ThrowsWhenNoSourceConfigured() {
        when(complianceService.validateSources(any())).thenReturn(List.of());

        LeadDiscoveryRequest request = new LeadDiscoveryRequest(
            "fornecedores",
            "SUPPLIER",
            3,
            null,
            1L
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.discoverLeads(request)
        );

        assertEquals(
            "No lead sources configured. Configure prospectos.leads.default-sources or provide sources in request",
            exception.getMessage()
        );
    }

    private DefaultLeadDiscoveryService createService(List<LeadDiscoverySource> sources, Long defaultIcpId) {
        return new DefaultLeadDiscoveryService(
            sources,
            icpDataService,
            scoringService,
            complianceService,
            new LeadSearchProperties(defaultIcpId),
            queryMetricsRecorder
        );
    }

    private DiscoveredLeadCandidate candidate(String name, String website, String industry) {
        return new DiscoveredLeadCandidate(
            name,
            website,
            industry,
            "Company description",
            "Sao Paulo, BR",
            List.of("contact@example.com"),
            "llm-discovery"
        );
    }

    private record StubLeadDiscoverySource(String sourceName, List<DiscoveredLeadCandidate> candidates)
        implements LeadDiscoverySource {

        @Override
        public List<DiscoveredLeadCandidate> discover(DiscoveryContext context) {
            return candidates;
        }
    }
}
