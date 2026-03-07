package dev.prospectos.ai.config;

import dev.prospectos.ai.exception.AIConfigurationException;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GroqChatModelConfigTest {

    @Test
    void groqChatModelThrowsWhenApiKeyIsMissing() {
        UrlNormalizationService urlNormalizationService = mock(UrlNormalizationService.class);
        GroqChatModelConfig config = new GroqChatModelConfig(urlNormalizationService);

        ReflectionTestUtils.setField(config, "groqApiKey", " ");
        ReflectionTestUtils.setField(config, "groqBaseUrl", "https://api.groq.com/openai");
        ReflectionTestUtils.setField(config, "groqModel", "llama-3.1-8b-instant");

        assertThatThrownBy(config::groqChatModel)
            .isInstanceOf(AIConfigurationException.class)
            .hasMessageContaining("Provider: groq")
            .hasMessageContaining("Key: api-key");
    }

    @Test
    void groqChatModelCreatesModelWhenConfigurationIsValid() {
        UrlNormalizationService urlNormalizationService = mock(UrlNormalizationService.class);
        when(urlNormalizationService.normalizeGroqUrl("https://api.groq.com/openai"))
            .thenReturn("https://api.groq.com/openai/v1");

        GroqChatModelConfig config = new GroqChatModelConfig(urlNormalizationService);
        ReflectionTestUtils.setField(config, "groqApiKey", "test-key");
        ReflectionTestUtils.setField(config, "groqBaseUrl", "https://api.groq.com/openai");
        ReflectionTestUtils.setField(config, "groqModel", "llama-3.1-8b-instant");

        ChatModel model = config.groqChatModel();

        assertThat(model).isNotNull();
    }

    @Test
    void groqChatModelWrapsInvalidConfigurationErrors() {
        UrlNormalizationService urlNormalizationService = mock(UrlNormalizationService.class);
        when(urlNormalizationService.normalizeGroqUrl("https://api.groq.com/openai"))
            .thenReturn(null);

        GroqChatModelConfig config = new GroqChatModelConfig(urlNormalizationService);
        ReflectionTestUtils.setField(config, "groqApiKey", "test-key");
        ReflectionTestUtils.setField(config, "groqBaseUrl", "https://api.groq.com/openai");
        ReflectionTestUtils.setField(config, "groqModel", "llama-3.1-8b-instant");

        assertThatThrownBy(config::groqChatModel)
            .isInstanceOf(AIConfigurationException.class)
            .hasMessageContaining("Provider: groq")
            .hasMessageContaining("Key: api-key");
    }
}
