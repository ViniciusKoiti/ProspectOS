package dev.prospectos.infrastructure.service.discovery;

import dev.prospectos.ai.config.VectorizationProperties;
import dev.prospectos.ai.vector.HashingTextEmbeddingService;
import dev.prospectos.ai.vector.InMemoryVectorIndex;
import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.dto.CompanyDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SemanticCompanySearchServiceTest {

    @Mock
    private CompanyDataService companyDataService;

    @Test
    void search_ReturnsSemanticMatchesFromKnownCompanies() {
        CompanyDTO agileSoft = new CompanyDTO(
            1L,
            "AgileSoft",
            "Software",
            "https://agilesoft.com",
            "Software consultancy using scrum and kanban with agile engineering teams",
            120,
            "Sao Paulo, BR",
            null
        );
        when(companyDataService.findCompany(1L)).thenReturn(agileSoft);

        VectorizationProperties properties = new VectorizationProperties(
            "in-memory",
            "hashing-v1",
            128,
            5,
            0.10d,
            null
        );
        HashingTextEmbeddingService embeddingService = new HashingTextEmbeddingService(properties);
        InMemoryVectorIndex vectorIndex = new InMemoryVectorIndex(embeddingService);
        vectorIndex.upsert(
            "company:1",
            "AgileSoft. Software consultancy using scrum and kanban with agile engineering teams",
            Map.of("companyId", 1L)
        );

        SemanticCompanySearchService service = new SemanticCompanySearchService(
            companyDataService,
            vectorIndex,
            properties
        );

        List<SemanticCompanyMatch> matches = service.search("agile scrum software teams", 3);

        assertFalse(matches.isEmpty());
        assertEquals("AgileSoft", matches.getFirst().company().name());
    }
}
