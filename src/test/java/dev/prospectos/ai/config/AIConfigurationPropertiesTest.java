package dev.prospectos.ai.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AI configuration properties constants.
 * Ensures constants are properly defined and accessible.
 */
class AIConfigurationPropertiesTest {

    @Test
    void shouldHaveCorrectAIEnabledProperty() {
        assertEquals("prospectos.ai.enabled", AIConfigurationProperties.AI_ENABLED);
    }

    @Test
    void shouldHaveCorrectGroqProperties() {
        assertEquals("prospectos.ai.groq.enabled", AIConfigurationProperties.GROQ_ENABLED);
        assertEquals("prospectos.ai.groq.api-key", AIConfigurationProperties.GROQ_API_KEY);
        assertEquals("prospectos.ai.groq.base-url", AIConfigurationProperties.GROQ_BASE_URL);
        assertEquals("prospectos.ai.groq.model", AIConfigurationProperties.GROQ_MODEL);
    }

    @Test
    void shouldHaveCorrectDefaultValues() {
        assertEquals("https://api.groq.com/openai", AIConfigurationProperties.DEFAULT_GROQ_BASE_URL);
        assertEquals("llama3-8b-8192", AIConfigurationProperties.DEFAULT_GROQ_MODEL);
        assertEquals("gpt-4-turbo-preview", AIConfigurationProperties.DEFAULT_OPENAI_MODEL);
        assertEquals("claude-3-5-sonnet-20241022", AIConfigurationProperties.DEFAULT_ANTHROPIC_MODEL);
    }

    @Test
    void shouldHaveProfileExclusions() {
        assertEquals("!test", AIConfigurationProperties.EXCLUDE_TEST_PROFILE);
    }

    @Test
    void shouldHavePrivateConstructor() {
        // Utility class should have private constructor
        try {
            var constructor = AIConfigurationProperties.class.getDeclaredConstructor();
            assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()), 
                      "Constructor should be private");
        } catch (NoSuchMethodException e) {
            fail("Private constructor should exist");
        }
    }
}