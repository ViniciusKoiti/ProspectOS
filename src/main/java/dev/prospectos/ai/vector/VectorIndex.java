package dev.prospectos.ai.vector;

import java.util.List;
import java.util.Map;

/**
 * Abstraction for storing vectors and running similarity search.
 */
public interface VectorIndex {

    int dimensions();

    void clear();

    void upsert(String id, float[] vector, Map<String, Object> metadata);

    List<VectorSearchMatch> similaritySearch(float[] queryVector, int topK, double minSimilarity);
}
