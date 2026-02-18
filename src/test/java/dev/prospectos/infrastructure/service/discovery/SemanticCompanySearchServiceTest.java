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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SemanticCompanySearchServiceTest {

    @Mock
    private CompanyDataService companyDataService;

    @Test
    void search_ReturnsSemanticMatchesFromKnownCompanies() {
        when(companyDataService.findAllCompanies()).thenReturn(List.of(
            new CompanyDTO(
                1L,
                "AgileSoft",
                "Software",
                "https://agilesoft.com",
                "Software consultancy using scrum and kanban with agile engineering teams",
                120,
                "Sao Paulo, BR",
                null
            ),
            new CompanyDTO(
                2L,
                "SteelWorks",
                "Manufacturing",
                "https://steelworks.com",
                "Heavy industry for steel production",
                800,
                "Belo Horizonte, BR",
                null
            )
        ));

        VectorizationProperties properties = new VectorizationProperties("hashing-v1", 128, 5, 0.10d);
        SemanticCompanySearchService service = new SemanticCompanySearchService(
            companyDataService,
            new HashingTextEmbeddingService(properties),
            new InMemoryVectorIndex(properties),
            properties
        );

        List<SemanticCompanyMatch> matches = service.search("agile scrum software teams", 3);

        assertFalse(matches.isEmpty());
        assertEquals("AgileSoft", matches.getFirst().company().name());
    }

    @Test
    void search_IsSafeUnderConcurrentAccess() throws ExecutionException, InterruptedException {
        when(companyDataService.findAllCompanies()).thenReturn(List.of(
            new CompanyDTO(
                1L,
                "AgileSoft",
                "Software",
                "https://agilesoft.com",
                "Software consultancy using scrum and kanban",
                120,
                "Sao Paulo, BR",
                null
            ),
            new CompanyDTO(
                2L,
                "CloudOps",
                "Software",
                "https://cloudops.com",
                "Cloud engineering and devops teams",
                90,
                "Curitiba, BR",
                null
            )
        ));

        VectorizationProperties properties = new VectorizationProperties("hashing-v1", 128, 5, 0.05d);
        SemanticCompanySearchService service = new SemanticCompanySearchService(
            companyDataService,
            new HashingTextEmbeddingService(properties),
            new InMemoryVectorIndex(properties),
            properties
        );

        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            Callable<List<SemanticCompanyMatch>> task = () -> service.search("agile cloud software", 2);
            List<Future<List<SemanticCompanyMatch>>> futures = List.of(
                executor.submit(task),
                executor.submit(task),
                executor.submit(task),
                executor.submit(task)
            );

            for (Future<List<SemanticCompanyMatch>> future : futures) {
                List<SemanticCompanyMatch> result = future.get();
                assertNotNull(result);
            }
        } finally {
            executor.shutdownNow();
        }
    }
}
