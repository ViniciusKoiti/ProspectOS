package dev.prospectos.ai.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class VectorizationEmbeddingConfig {

    @Bean("vectorizationEmbeddingModel")
    @Primary
    @ConditionalOnProperty(prefix = "prospectos.vectorization", name = "backend", havingValue = "pgvector")
    public EmbeddingModel vectorizationEmbeddingModel(
        VectorizationProperties properties,
        @Qualifier("openAiEmbeddingModel") @Autowired(required = false) EmbeddingModel openAiEmbeddingModel,
        @Qualifier("groqEmbeddingModel") @Autowired(required = false) EmbeddingModel groqEmbeddingModel
    ) {
        return switch (properties.embeddingProvider()) {
            case "openai" -> require("openai", openAiEmbeddingModel);
            case "groq" -> require("groq", groqEmbeddingModel);
            default -> throw new IllegalStateException(
                "Unsupported vectorization embedding provider: " + properties.embeddingProvider()
            );
        };
    }

    private EmbeddingModel require(String provider, EmbeddingModel embeddingModel) {
        if (embeddingModel != null) {
            return embeddingModel;
        }
        throw new IllegalStateException(
            "Vector backend 'pgvector' requires the selected embedding provider '" + provider
                + "' to expose an EmbeddingModel bean."
        );
    }
}
