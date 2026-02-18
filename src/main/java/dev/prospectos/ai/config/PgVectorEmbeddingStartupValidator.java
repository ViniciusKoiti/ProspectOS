package dev.prospectos.ai.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Fail-fast validator for pgvector backend requirements.
 */
@Component
@ConditionalOnProperty(prefix = "prospectos.vectorization", name = "backend", havingValue = "pgvector")
public class PgVectorEmbeddingStartupValidator {

    public PgVectorEmbeddingStartupValidator(ObjectProvider<EmbeddingModel> embeddingModelProvider) {
        if (embeddingModelProvider.getIfAvailable() == null) {
            throw new IllegalStateException(
                "Vector backend 'pgvector' requires an EmbeddingModel bean. "
                    + "Configure a Spring AI embedding provider/model before enabling "
                    + "'prospectos.vectorization.backend=pgvector'."
            );
        }
    }
}
