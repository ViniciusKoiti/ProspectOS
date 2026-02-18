package dev.prospectos.ai.vector;

import dev.prospectos.ai.config.VectorizationProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpringAiVectorStoreIndexTest {

    @Mock
    private VectorStore vectorStore;

    @Test
    void upsert_DeletesBeforeInsert() {
        VectorizationProperties properties = new VectorizationProperties("pgvector", "test", 128, 5, 0.2d, null);
        SpringAiVectorStoreIndex index = new SpringAiVectorStoreIndex(vectorStore, properties);

        index.upsert("company:10", "Acme software", Map.of("companyId", 10L));

        verify(vectorStore).delete(List.of("company:10"));
        verify(vectorStore).add(any());
    }

    @Test
    void similaritySearch_ReturnsMappedMatches() {
        VectorizationProperties properties = new VectorizationProperties("pgvector", "test", 128, 5, 0.2d, null);
        SpringAiVectorStoreIndex index = new SpringAiVectorStoreIndex(vectorStore, properties);

        Document doc = Document.builder()
            .withId("company:1")
            .withContent("AgileSoft")
            .withMetadata(Map.of("companyId", 1L, "score", 0.93d))
            .build();
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc));

        List<VectorSearchMatch> matches = index.similaritySearch("agile software", 3, 0.2d);

        assertFalse(matches.isEmpty());
        assertEquals("company:1", matches.getFirst().id());
        assertEquals(1L, matches.getFirst().metadata().get("companyId"));
    }
}
