package dev.prospectos.ai.vector;

import java.util.List;
import java.util.Map;

/**
 * Abstraction for storing vectors and running similarity search.
 */
public interface VectorIndex {

    int dimensions();

    void clear();

    void upsert(String id, String content, Map<String, Object> metadata);

    void delete(String id);

    List<VectorSearchMatch> similaritySearch(String query, int topK, double minSimilarity);
}
