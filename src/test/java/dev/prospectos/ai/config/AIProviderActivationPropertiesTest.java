package dev.prospectos.ai.config;

import dev.prospectos.ai.client.LLMProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AIProviderActivationPropertiesTest {

    @Test
    void shouldUseDefaultProvidersWhenBlank() {
        AIProviderActivationProperties properties = new AIProviderActivationProperties("");

        assertEquals(
            java.util.List.of(LLMProvider.OPENAI, LLMProvider.ANTHROPIC),
            properties.activeProviders()
        );
    }

    @Test
    void shouldNormalizeAndDeduplicateConfiguredProviders() {
        AIProviderActivationProperties properties =
            new AIProviderActivationProperties(" groq , openai,groq , anthropic ");

        assertEquals(
            java.util.List.of(LLMProvider.GROQ, LLMProvider.OPENAI, LLMProvider.ANTHROPIC),
            properties.activeProviders()
        );
        assertTrue(properties.isActive(LLMProvider.GROQ));
    }

    @Test
    void shouldRejectUnknownProviderNames() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new AIProviderActivationProperties("openai,unknown-provider")
        );

        assertTrue(exception.getMessage().contains("prospectos.ai.active-providers"));
    }
}
