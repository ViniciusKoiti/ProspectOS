package dev.prospectos.ai.config;

import dev.prospectos.ai.exception.AIConfigurationException;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GroqEmbeddingConfigTest {

    @Test
    void groqEmbeddingModelThrowsWhenApiKeyIsMissing() {
        UrlNormalizationService urlNormalizationService = mock(UrlNormalizationService.class);
        GroqEmbeddingConfig config = new GroqEmbeddingConfig(urlNormalizationService);

        ReflectionTestUtils.setField(config, "groqApiKey", "");
        ReflectionTestUtils.setField(config, "groqBaseUrl", "https://api.groq.com/openai");

        assertThatThrownBy(config::groqEmbeddingModel)
            .isInstanceOf(AIConfigurationException.class)
            .hasMessageContaining("Provider: groq")
            .hasMessageContaining("Key: api-key");
    }

    @Test
    void groqEmbeddingModelCreatesModelWhenConfigurationIsValid() {
        UrlNormalizationService urlNormalizationService = mock(UrlNormalizationService.class);
        when(urlNormalizationService.normalizeGroqUrl("https://api.groq.com/openai"))
            .thenReturn("https://api.groq.com/openai/v1");

        GroqEmbeddingConfig config = new GroqEmbeddingConfig(urlNormalizationService);
        ReflectionTestUtils.setField(config, "groqApiKey", "test-key");
        ReflectionTestUtils.setField(config, "groqBaseUrl", "https://api.groq.com/openai");

        EmbeddingModel model = config.groqEmbeddingModel();

        assertThat(model).isNotNull();
    }

    @Test
    void groqEmbeddingModelWrapsInvalidConfigurationErrors() {
        UrlNormalizationService urlNormalizationService = mock(UrlNormalizationService.class);
        when(urlNormalizationService.normalizeGroqUrl("https://api.groq.com/openai"))
            .thenReturn(null);

        GroqEmbeddingConfig config = new GroqEmbeddingConfig(urlNormalizationService);
        ReflectionTestUtils.setField(config, "groqApiKey", "test-key");
        ReflectionTestUtils.setField(config, "groqBaseUrl", "https://api.groq.com/openai");

        assertThatThrownBy(config::groqEmbeddingModel)
            .isInstanceOf(AIConfigurationException.class)
            .hasMessageContaining("Provider: groq")
            .hasMessageContaining("Key: api-key");
    }
}
