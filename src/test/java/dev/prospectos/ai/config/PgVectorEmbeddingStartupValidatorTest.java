package dev.prospectos.ai.config;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PgVectorEmbeddingStartupValidatorTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(PgVectorEmbeddingStartupValidator.class);

    @Test
    void failsWhenPgvectorBackendEnabledAndEmbeddingModelMissing() {
        contextRunner
            .withPropertyValues("prospectos.vectorization.backend=pgvector")
            .run(context -> {
                assertThat(context).hasFailed();
                Throwable startupFailure = context.getStartupFailure();
                assertThat(startupFailure).isNotNull();
                Throwable rootCause = startupFailure;
                while (rootCause.getCause() != null) {
                    rootCause = rootCause.getCause();
                }
                assertThat(rootCause).isInstanceOf(IllegalStateException.class);
                assertThat(rootCause.getMessage()).contains("requires an EmbeddingModel bean");
            });
    }

    @Test
    void startsWhenPgvectorBackendEnabledAndEmbeddingModelProvided() {
        contextRunner
            .withUserConfiguration(TestEmbeddingConfiguration.class)
            .withPropertyValues("prospectos.vectorization.backend=pgvector")
            .run(context -> {
                assertThat(context).hasNotFailed();
                assertThat(context).hasSingleBean(PgVectorEmbeddingStartupValidator.class);
            });
    }

    @Configuration
    static class TestEmbeddingConfiguration {
        @Bean
        EmbeddingModel embeddingModel() {
            return new EmbeddingModel() {
                @Override
                public EmbeddingResponse call(EmbeddingRequest request) {
                    List<Embedding> embeddings = new ArrayList<>();
                    int size = request == null || request.getInstructions() == null ? 0 : request.getInstructions().size();
                    for (int i = 0; i < size; i++) {
                        embeddings.add(new Embedding(new float[] {1.0f, 0.0f}, i));
                    }
                    return new EmbeddingResponse(embeddings);
                }

                @Override
                public float[] embed(Document document) {
                    return new float[] {1.0f, 0.0f};
                }
            };
        }
    }
}
