package dev.prospectos.ai.vector;

import dev.prospectos.ai.config.VectorizationProperties;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory vector index for semantic search.
 */
@Component
public class InMemoryVectorIndex implements VectorIndex {

    private final int dimensions;
    private final Map<String, Entry> entries = new ConcurrentHashMap<>();

    public InMemoryVectorIndex(VectorizationProperties properties) {
        this.dimensions = properties.embeddingDimension();
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
    public void upsert(String id, float[] vector, Map<String, Object> metadata) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Vector id cannot be blank");
        }
        validateDimensions(vector);
        entries.put(id, new Entry(vector.clone(), metadata == null ? Map.of() : Map.copyOf(metadata)));
    }

    @Override
    public List<VectorSearchMatch> similaritySearch(float[] queryVector, int topK, double minSimilarity) {
        validateDimensions(queryVector);
        if (topK <= 0 || entries.isEmpty()) {
            return List.of();
        }

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

    private void validateDimensions(float[] vector) {
        if (vector == null || vector.length != dimensions) {
            throw new IllegalArgumentException(
                "Vector dimension mismatch. Expected " + dimensions + " but got " + (vector == null ? 0 : vector.length)
            );
        }
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
