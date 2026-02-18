package dev.prospectos.infrastructure.service.discovery;

import dev.prospectos.ai.config.VectorizationProperties;
import dev.prospectos.ai.vector.TextEmbeddingService;
import dev.prospectos.ai.vector.VectorIndex;
import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ScoreDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class CompanyVectorIndexingServiceTest {

    @Mock
    private CompanyDataService companyDataService;

    @Mock
    private TextEmbeddingService embeddingService;

    @Mock
    private VectorIndex vectorIndex;

    @Test
    void reindexCompany_UpsertsSemanticDocumentWhenCompanyExists() {
        when(companyDataService.findCompany(1L)).thenReturn(new CompanyDTO(
            1L,
            "TechCorp",
            "Software",
            "https://techcorp.com",
            "Cloud engineering and agile teams",
            120,
            "Sao Paulo, BR",
            new ScoreDTO(80, "HOT", "Fit")
        ));
        when(embeddingService.descriptor()).thenReturn(new dev.prospectos.ai.vector.EmbeddingModelDescriptor("mock", 128));

        VectorizationProperties properties = new VectorizationProperties("in-memory", "mock", 128, 5, 0.2d, null);
        CompanyVectorIndexingService service = new CompanyVectorIndexingService(
            companyDataService,
            embeddingService,
            vectorIndex,
            properties
        );

        service.reindexCompany(1L);

        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        verify(vectorIndex).upsert(
            org.mockito.ArgumentMatchers.eq(vectorId(1L)),
            contentCaptor.capture(),
            org.mockito.ArgumentMatchers.anyMap()
        );
        assertTrue(contentCaptor.getValue().contains("TechCorp"));
    }

    @Test
    void reindexCompany_DeletesVectorWhenCompanyNoLongerExists() {
        when(companyDataService.findCompany(2L)).thenReturn(null);
        VectorizationProperties properties = new VectorizationProperties("in-memory", "mock", 128, 5, 0.2d, null);
        CompanyVectorIndexingService service = new CompanyVectorIndexingService(
            companyDataService,
            embeddingService,
            vectorIndex,
            properties
        );

        service.reindexCompany(2L);

        verify(vectorIndex).delete(vectorId(2L));
    }

    private String vectorId(Long companyId) {
        return UUID.nameUUIDFromBytes(("company:" + companyId).getBytes(StandardCharsets.UTF_8)).toString();
    }
}
