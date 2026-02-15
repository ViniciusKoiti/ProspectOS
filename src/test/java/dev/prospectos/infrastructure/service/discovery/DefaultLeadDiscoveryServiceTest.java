package dev.prospectos.infrastructure.service.discovery;

import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.dto.LeadDiscoveryRequest;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultLeadDiscoveryServiceTest {

    @Mock
    private ICPDataService icpDataService;

    @Mock
    private CompanyScoringService scoringService;

    @Mock
    private AllowedSourcesComplianceService complianceService;

    private DefaultLeadDiscoveryService service;

    @BeforeEach
    void setUp() {
        service = new DefaultLeadDiscoveryService(
            List.of(),
            icpDataService,
            scoringService,
            complianceService,
            new LeadSearchProperties(1L)
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
}
