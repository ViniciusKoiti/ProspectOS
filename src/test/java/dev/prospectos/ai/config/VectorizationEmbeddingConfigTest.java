package dev.prospectos.ai.config;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VectorizationEmbeddingConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AIProviderCredentialsConfig.class))
        .withUserConfiguration(
            VectorizationEmbeddingConfig.class,
            TestEmbeddingConfiguration.class,
            TestVectorizationPropertiesConfiguration.class
        );

    @Test
    void selectsOpenAiEmbeddingWhenConfigured() {
        contextRunner
            .withPropertyValues(
                "prospectos.vectorization.backend=pgvector",
                "prospectos.vectorization.embedding-provider=openai"
            )
            .run(context -> {
                assertThat(context).hasNotFailed();
                assertThat(context).getBean("vectorizationEmbeddingModel").isSameAs(context.getBean("openAiEmbeddingModel"));
            });
    }

    @Test
    void selectsGroqEmbeddingWhenConfigured() {
        contextRunner
            .withPropertyValues(
                "prospectos.vectorization.backend=pgvector",
                "prospectos.vectorization.embedding-provider=groq"
            )
            .run(context -> {
                assertThat(context).hasNotFailed();
                assertThat(context).getBean("vectorizationEmbeddingModel").isSameAs(context.getBean("groqEmbeddingModel"));
            });
    }

    @Test
    void failsWhenSelectedEmbeddingProviderBeanIsMissing() {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AIProviderCredentialsConfig.class))
            .withUserConfiguration(
                VectorizationEmbeddingConfig.class,
                OpenAiOnlyEmbeddingConfiguration.class,
                TestVectorizationPropertiesConfiguration.class
            )
            .withPropertyValues(
                "prospectos.vectorization.backend=pgvector",
                "prospectos.vectorization.embedding-provider=groq"
            )
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure()).hasMessageContaining("selected embedding provider 'groq'");
            });
    }

    @Configuration
    static class TestEmbeddingConfiguration {
        @Bean("openAiEmbeddingModel")
        EmbeddingModel openAiEmbeddingModel() {
            return testEmbeddingModel();
        }

        @Bean("groqEmbeddingModel")
        EmbeddingModel groqEmbeddingModel() {
            return testEmbeddingModel();
        }
    }

    @Configuration
    static class OpenAiOnlyEmbeddingConfiguration {
        @Bean("openAiEmbeddingModel")
        EmbeddingModel openAiEmbeddingModel() {
            return testEmbeddingModel();
        }
    }

    @Configuration
    static class TestVectorizationPropertiesConfiguration {
        @Bean
        VectorizationProperties vectorizationProperties(org.springframework.core.env.Environment environment) {
            return new VectorizationProperties(
                environment.getProperty("prospectos.vectorization.backend"),
                environment.getProperty("prospectos.vectorization.model-id"),
                256,
                5,
                0.2d,
                null,
                environment.getProperty("prospectos.vectorization.embedding-provider")
            );
        }
    }

    private static EmbeddingModel testEmbeddingModel() {
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
