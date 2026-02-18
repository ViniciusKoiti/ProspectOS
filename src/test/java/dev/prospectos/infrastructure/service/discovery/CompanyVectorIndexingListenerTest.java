package dev.prospectos.infrastructure.service.discovery;

import dev.prospectos.ai.config.VectorizationProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class CompanyVectorIndexingListenerTest {

    @Mock
    private CompanyVectorIndexingService indexingService;

    @Test
    void onCompanyChanged_DoesNotPropagateIndexingFailure() {
        doThrow(new IllegalStateException("vector down")).when(indexingService).reindexCompany(10L);

        VectorizationProperties properties = new VectorizationProperties("pgvector", "hashing-v1", 128, 5, 0.2d, null);
        CompanyVectorIndexingListener listener = new CompanyVectorIndexingListener(indexingService, properties);

        assertDoesNotThrow(() -> listener.onCompanyChanged(new CompanyVectorReindexRequested(10L)));
    }
}
