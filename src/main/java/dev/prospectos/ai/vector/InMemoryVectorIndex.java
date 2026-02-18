package dev.prospectos.ai.vector;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory vector index for semantic search.
 */
@Component
@ConditionalOnProperty(
    prefix = "prospectos.vectorization",
    name = "backend",
    havingValue = "in-memory",
    matchIfMissing = true
)
public class InMemoryVectorIndex implements VectorIndex {

    private final TextEmbeddingService embeddingService;
    private final int dimensions;
    private final Map<String, Entry> entries = new ConcurrentHashMap<>();

    public InMemoryVectorIndex(TextEmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
        this.dimensions = embeddingService.descriptor().dimensions();
    }

    @Override
    public int dimensions() {
        return dimensions;
    }

    @Override
    public void clear() {
        entries.clear();
    }

    @Override
    public void upsert(String id, String content, Map<String, Object> metadata) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Vector id cannot be blank");
        }
        float[] vector = embeddingService.embed(content);
        entries.put(id, new Entry(vector.clone(), metadata == null ? Map.of() : Map.copyOf(metadata)));
    }

    @Override
    public void delete(String id) {
        if (id == null || id.isBlank()) {
            return;
        }
        entries.remove(id);
    }

    @Override
    public List<VectorSearchMatch> similaritySearch(String query, int topK, double minSimilarity) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        if (topK <= 0 || entries.isEmpty()) {
            return List.of();
        }

        float[] queryVector = embeddingService.embed(query);
        return entries.entrySet().stream()
            .map(entry -> new VectorSearchMatch(
                entry.getKey(),
                cosineSimilarity(queryVector, entry.getValue().vector()),
                entry.getValue().metadata()
            ))
            .filter(match -> match.similarity() >= minSimilarity)
            .sorted(Comparator.comparingDouble(VectorSearchMatch::similarity).reversed())
            .limit(topK)
            .toList();
    }

    private double cosineSimilarity(float[] left, float[] right) {
        double dot = 0.0d;
        double leftNorm = 0.0d;
        double rightNorm = 0.0d;

        for (int i = 0; i < left.length; i++) {
            dot += left[i] * right[i];
            leftNorm += left[i] * left[i];
            rightNorm += right[i] * right[i];
        }

        if (leftNorm == 0.0d || rightNorm == 0.0d) {
            return 0.0d;
        }
        return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }

    private record Entry(float[] vector, Map<String, Object> metadata) {
    }
}
