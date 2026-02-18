package dev.prospectos.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Vectorization configuration for embedding and semantic search.
 */
@ConfigurationProperties(prefix = "prospectos.vectorization")
public record VectorizationProperties(
    String modelId,
    Integer embeddingDimension,
    Integer topK,
    Double minSimilarity
) {
    private static final String DEFAULT_MODEL_ID = "hashing-v1";
    private static final int DEFAULT_EMBEDDING_DIMENSION = 256;
    private static final int DEFAULT_TOP_K = 5;
    private static final double DEFAULT_MIN_SIMILARITY = 0.20d;

    public VectorizationProperties {
        modelId = modelId == null || modelId.isBlank() ? DEFAULT_MODEL_ID : modelId.trim();
        embeddingDimension = embeddingDimension == null ? DEFAULT_EMBEDDING_DIMENSION : embeddingDimension;
        topK = topK == null ? DEFAULT_TOP_K : topK;
        minSimilarity = minSimilarity == null ? DEFAULT_MIN_SIMILARITY : minSimilarity;

        if (embeddingDimension <= 0) {
            throw new IllegalArgumentException("prospectos.vectorization.embedding-dimension must be > 0");
        }
        if (topK <= 0) {
            throw new IllegalArgumentException("prospectos.vectorization.top-k must be > 0");
        }
        if (minSimilarity < 0.0d || minSimilarity > 1.0d) {
            throw new IllegalArgumentException("prospectos.vectorization.min-similarity must be between 0 and 1");
        }
    }
}
