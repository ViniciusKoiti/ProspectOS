package dev.prospectos.ai.vector;

import dev.prospectos.ai.config.VectorizationProperties;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Vector index adapter backed by Spring AI VectorStore.
 */
@Component
@ConditionalOnProperty(prefix = "prospectos.vectorization", name = "backend", havingValue = "pgvector")
public class SpringAiVectorStoreIndex implements VectorIndex {

    private final VectorStore vectorStore;
    private final int dimensions;

    public SpringAiVectorStoreIndex(VectorStore vectorStore, VectorizationProperties properties) {
        this.vectorStore = vectorStore;
        this.dimensions = properties.embeddingDimension();
    }

    @Override
    public int dimensions() {
        return dimensions;
    }

    @Override
    public void clear() {
        // No-op for persistent stores in incremental indexing mode.
    }

    @Override
    public void upsert(String id, String content, Map<String, Object> metadata) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Vector id cannot be blank");
        }
        delete(id);

        Map<String, Object> safeMetadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        Document document = Document.builder()
            .withId(id)
            .withContent(content == null ? "" : content)
            .withMetadata(safeMetadata)
            .build();
        vectorStore.add(List.of(document));
    }

    @Override
    public void delete(String id) {
        if (id == null || id.isBlank()) {
            return;
        }
        vectorStore.delete(List.of(id));
    }

    @Override
    public List<VectorSearchMatch> similaritySearch(String query, int topK, double minSimilarity) {
        if (query == null || query.isBlank() || topK <= 0) {
            return List.of();
        }

        SearchRequest request = SearchRequest.query(query)
            .withTopK(topK)
            .withSimilarityThreshold(minSimilarity);

        List<Document> documents = vectorStore.similaritySearch(request);
        if (documents == null || documents.isEmpty()) {
            return List.of();
        }

        return documents.stream()
            .map(document -> new VectorSearchMatch(
                document.getId(),
                extractScore(document),
                document.getMetadata()
            ))
            .toList();
    }

    private double extractScore(Document document) {
        if (document == null || document.getMetadata() == null) {
            return 0.0d;
        }
        Object score = document.getMetadata().get("score");
        if (score instanceof Number number) {
            return number.doubleValue();
        }
        return 0.0d;
    }
}
