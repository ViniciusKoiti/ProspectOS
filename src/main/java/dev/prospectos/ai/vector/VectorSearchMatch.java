package dev.prospectos.ai.vector;

import java.util.Map;

/**
 * Single similarity search match from vector index.
 */
public record VectorSearchMatch(
    String id,
    double similarity,
    Map<String, Object> metadata
) {
}
