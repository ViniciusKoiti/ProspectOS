package dev.prospectos.infrastructure.service.discovery;

import dev.prospectos.api.dto.CompanyDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VectorCompanyLeadDiscoverySourceTest {

    @Mock
    private SemanticCompanySearchService semanticSearchService;

    @Test
    void discover_DoesNotAppendSimilarityIntoBusinessDescription() {
        CompanyDTO company = new CompanyDTO(
            1L,
            "AgileSoft",
            "Software",
            "https://agilesoft.com",
            "Consultoria agile",
            120,
            "Sao Paulo, BR",
            null
        );

        when(semanticSearchService.search("agile", 3))
            .thenReturn(List.of(new SemanticCompanyMatch(company, 0.9876d)));

        VectorCompanyLeadDiscoverySource source = new VectorCompanyLeadDiscoverySource(semanticSearchService);
        DiscoveryContext context = new DiscoveryContext("agile", "SUPPLIER", 3, null);

        List<DiscoveredLeadCandidate> results = source.discover(context);
        String description = results.getFirst().description();

        assertEquals("Consultoria agile", description);
    }
}
