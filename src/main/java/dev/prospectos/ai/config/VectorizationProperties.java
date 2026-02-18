package dev.prospectos.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Vectorization configuration for embedding and semantic search.
 */
@ConfigurationProperties(prefix = "prospectos.vectorization")
public record VectorizationProperties(
    String backend,
    String modelId,
    Integer embeddingDimension,
    Integer topK,
    Double minSimilarity,
    PgVectorProperties pgvector
) {
    private static final String DEFAULT_BACKEND = "in-memory";
    private static final String DEFAULT_MODEL_ID = "hashing-v1";
    private static final int DEFAULT_EMBEDDING_DIMENSION = 256;
    private static final int DEFAULT_TOP_K = 5;
    private static final double DEFAULT_MIN_SIMILARITY = 0.20d;
    private static final String DEFAULT_PGVECTOR_TABLE_NAME = "company_vectors";
    private static final boolean DEFAULT_PGVECTOR_INITIALIZE_SCHEMA = true;

    public VectorizationProperties {
        backend = backend == null || backend.isBlank() ? DEFAULT_BACKEND : backend.trim().toLowerCase();
        modelId = modelId == null || modelId.isBlank() ? DEFAULT_MODEL_ID : modelId.trim();
        embeddingDimension = embeddingDimension == null ? DEFAULT_EMBEDDING_DIMENSION : embeddingDimension;
        topK = topK == null ? DEFAULT_TOP_K : topK;
        minSimilarity = minSimilarity == null ? DEFAULT_MIN_SIMILARITY : minSimilarity;
        pgvector = pgvector == null
            ? new PgVectorProperties(DEFAULT_PGVECTOR_TABLE_NAME, DEFAULT_PGVECTOR_INITIALIZE_SCHEMA)
            : pgvector;

        if (embeddingDimension <= 0) {
            throw new IllegalArgumentException("prospectos.vectorization.embedding-dimension must be > 0");
        }
        if (topK <= 0) {
            throw new IllegalArgumentException("prospectos.vectorization.top-k must be > 0");
        }
        if (minSimilarity < 0.0d || minSimilarity > 1.0d) {
            throw new IllegalArgumentException("prospectos.vectorization.min-similarity must be between 0 and 1");
        }
        if (!"in-memory".equals(backend) && !"pgvector".equals(backend)) {
            throw new IllegalArgumentException("prospectos.vectorization.backend must be one of: in-memory, pgvector");
        }
    }

    public record PgVectorProperties(
        String tableName,
        Boolean initializeSchema
    ) {
        public PgVectorProperties {
            tableName = tableName == null || tableName.isBlank() ? DEFAULT_PGVECTOR_TABLE_NAME : tableName.trim();
            initializeSchema = initializeSchema == null ? DEFAULT_PGVECTOR_INITIALIZE_SCHEMA : initializeSchema;
        }
    }
}
