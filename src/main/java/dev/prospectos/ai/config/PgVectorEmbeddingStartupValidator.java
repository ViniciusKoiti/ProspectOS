package dev.prospectos.ai.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Fail-fast validator for pgvector backend requirements.
 */
@Component
@ConditionalOnProperty(prefix = "prospectos.vectorization", name = "backend", havingValue = "pgvector")
public class PgVectorEmbeddingStartupValidator {

    public PgVectorEmbeddingStartupValidator(
        @Qualifier("vectorizationEmbeddingModel") ObjectProvider<EmbeddingModel> embeddingModelProvider
    ) {
        if (embeddingModelProvider.getIfAvailable() == null) {
            throw new IllegalStateException(
                "Vector backend 'pgvector' requires the selected vectorization embedding model. "
                    + "Configure 'prospectos.vectorization.embedding-provider' and ensure the matching "
                    + "EmbeddingModel bean is available before enabling 'prospectos.vectorization.backend=pgvector'."
            );
        }
    }
}
