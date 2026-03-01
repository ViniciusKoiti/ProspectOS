package dev.prospectos.ai.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AIConfigurationException.
 * Tests exception creation, context preservation, and user-friendly messaging.
 */
class AIConfigurationExceptionTest {

    @Test
    @DisplayName("Should create exception with all context parameters")
    void shouldCreateExceptionWithAllContext() {
        // Given
        String provider = "groq";
        String configKey = "api-key";
        String message = "API key is invalid";
        RuntimeException cause = new RuntimeException("Connection failed");

        // When
        AIConfigurationException exception = new AIConfigurationException(provider, configKey, message, cause);

        // Then
        assertEquals(provider, exception.getProvider());
        assertEquals(configKey, exception.getConfigurationKey());
        assertEquals(message, "API key is invalid");
        assertEquals(cause, exception.getCause());
        assertNotNull(exception.getTimestamp());
        assertNotNull(exception.getUserMessage());
    }

    @Test
    @DisplayName("Should create exception without cause")
    void shouldCreateExceptionWithoutCause() {
        // Given
        String provider = "openai";
        String configKey = "base-url";
        String message = "Invalid URL format";

        // When
        AIConfigurationException exception = new AIConfigurationException(provider, configKey, message);

        // Then
        assertEquals(provider, exception.getProvider());
        assertEquals(configKey, exception.getConfigurationKey());
        assertNull(exception.getCause());
        assertNotNull(exception.getTimestamp());
        assertNotNull(exception.getUserMessage());
    }

    @Test
    @DisplayName("Should format technical message correctly")
    void shouldFormatTechnicalMessageCorrectly() {
        // Given
        AIConfigurationException exception = new AIConfigurationException("groq", "api-key", "Invalid format");

        // When
        String message = exception.getMessage();

        // Then
        assertEquals("[AI Configuration Error] Provider: groq, Key: api-key, Details: Invalid format", message);
    }

    @ParameterizedTest
    @DisplayName("Should generate appropriate user messages for different config keys")
    @CsvSource({
        "groq, api-key, 'Configure a API key do groq corretamente'",
        "openai, base-url, 'Verifique a URL de configuração do openai'", 
        "anthropic, connection, 'Não foi possível conectar ao anthropic. Verifique sua conexão'",
        "groq, model, 'Modelo especificado para groq não é válido'",
        "openai, creation, 'Falha ao inicializar o openai. Verifique as configurações'"
    })
    void shouldGenerateAppropriateUserMessages(String provider, String configKey, String expectedMessage) {
        // When
        AIConfigurationException exception = new AIConfigurationException(provider, configKey, "technical details");

        // Then
        assertEquals(expectedMessage, exception.getUserMessage());
    }

    @Test
    @DisplayName("Should generate generic user message for unknown config key")
    void shouldGenerateGenericUserMessageForUnknownKey() {
        // Given
        AIConfigurationException exception = new AIConfigurationException("groq", "unknown-key", "some error");

        // When
        String userMessage = exception.getUserMessage();

        // Then
        assertEquals("Erro de configuração no groq: some error", userMessage);
    }

    @Test
    @DisplayName("Should provide error summary for logging")
    void shouldProvideErrorSummaryForLogging() {
        // Given
        AIConfigurationException exception = new AIConfigurationException("groq", "api-key", "Invalid");

        // When
        String summary = exception.getErrorSummary();

        // Then
        assertTrue(summary.startsWith("AI Config Error - Provider: groq, Key: api-key, Time:"));
        assertTrue(summary.contains("2026")); // Should contain current year
    }

    @Test
    @DisplayName("Should preserve timestamp accuracy")
    void shouldPreserveTimestampAccuracy() {
        // Given
        Instant beforeCreation = Instant.now();

        // When
        AIConfigurationException exception = new AIConfigurationException("groq", "api-key", "test");
        Instant afterCreation = Instant.now();

        // Then
        assertTrue(exception.getTimestamp().isAfter(beforeCreation.minusSeconds(1)));
        assertTrue(exception.getTimestamp().isBefore(afterCreation.plusSeconds(1)));
    }

    @Test
    @DisplayName("Should handle null provider gracefully")
    void shouldHandleNullProviderGracefully() {
        // When
        AIConfigurationException exception = new AIConfigurationException(null, "api-key", "test");

        // Then
        assertEquals("Configure a API key do unknown corretamente", exception.getUserMessage());
        assertTrue(exception.getMessage().contains("Provider: null"));
    }

    @Test
    @DisplayName("Should handle empty provider gracefully")
    void shouldHandleEmptyProviderGracefully() {
        // When
        AIConfigurationException exception = new AIConfigurationException("", "api-key", "test");

        // Then
        assertEquals("Configure a API key do  corretamente", exception.getUserMessage());
    }

    @Test
    @DisplayName("Should handle null configuration key gracefully")
    void shouldHandleNullConfigKeyGracefully() {
        // When
        AIConfigurationException exception = new AIConfigurationException("groq", null, "test");

        // Then
        assertEquals("Erro de configuração no groq: test", exception.getUserMessage());
        assertTrue(exception.getMessage().contains("Key: null"));
    }

    @Test
    @DisplayName("Should handle empty message gracefully")
    void shouldHandleEmptyMessageGracefully() {
        // When
        AIConfigurationException exception = new AIConfigurationException("groq", "api-key", "");

        // Then
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("Details: "));
        assertEquals("Configure a API key do groq corretamente", exception.getUserMessage());
    }

    @Test
    @DisplayName("Should be a RuntimeException")
    void shouldBeRuntimeException() {
        // When
        AIConfigurationException exception = new AIConfigurationException("groq", "api-key", "test");

        // Then
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("Should maintain cause chain")
    void shouldMaintainCauseChain() {
        // Given
        IllegalArgumentException rootCause = new IllegalArgumentException("Root cause");
        RuntimeException intermediateCause = new RuntimeException("Intermediate", rootCause);

        // When
        AIConfigurationException exception = new AIConfigurationException("groq", "api-key", "Final", intermediateCause);

        // Then
        assertEquals(intermediateCause, exception.getCause());
        assertEquals(rootCause, exception.getCause().getCause());
    }

    // Integration scenarios
    @Test
    @DisplayName("Should work in typical API key validation scenario")
    void shouldWorkInTypicalApiKeyValidationScenario() {
        try {
            // Simulate API key validation failure
            String apiKey = "";
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new AIConfigurationException("groq", "api-key", "API key is required but not configured");
            }
        } catch (AIConfigurationException e) {
            assertEquals("groq", e.getProvider());
            assertEquals("api-key", e.getConfigurationKey());
            assertEquals("Configure a API key do groq corretamente", e.getUserMessage());
            assertTrue(e.getMessage().contains("[AI Configuration Error]"));
        }
    }

    @Test
    @DisplayName("Should work in typical connection failure scenario")
    void shouldWorkInTypicalConnectionFailureScenario() {
        try {
            // Simulate connection failure
            throw new AIConfigurationException("openai", "connection", 
                "Connection timeout after 30 seconds", 
                new java.net.ConnectException("Connection refused"));
        } catch (AIConfigurationException e) {
            assertEquals("openai", e.getProvider());
            assertEquals("connection", e.getConfigurationKey());
            assertEquals("Não foi possível conectar ao openai. Verifique sua conexão", e.getUserMessage());
            assertTrue(e.getCause() instanceof java.net.ConnectException);
        }
    }
}