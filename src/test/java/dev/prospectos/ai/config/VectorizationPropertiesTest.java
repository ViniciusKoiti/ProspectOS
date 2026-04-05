package dev.prospectos.ai.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VectorizationPropertiesTest {

    @Test
    void defaultsEmbeddingProviderToOpenAi() {
        VectorizationProperties properties = new VectorizationProperties("pgvector", "hashing-v1", 256, 5, 0.2d, null, null);

        assertThat(properties.embeddingProvider()).isEqualTo("openai");
    }

    @Test
    void rejectsUnknownEmbeddingProvider() {
        assertThatThrownBy(() -> new VectorizationProperties(
            "pgvector",
            "hashing-v1",
            256,
            5,
            0.2d,
            null,
            "anthropic"
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("embedding-provider must be one of: openai, groq");
    }
}
