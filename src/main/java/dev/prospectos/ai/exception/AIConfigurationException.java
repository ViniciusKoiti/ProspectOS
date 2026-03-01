package dev.prospectos.ai.exception;

import java.time.Instant;

/**
 * Exception thrown when AI configuration fails.
 * Provides detailed context about the configuration failure.
 */
public class AIConfigurationException extends RuntimeException {

    private final String provider;
    private final String configurationKey;
    private final String userMessage;
    private final Instant timestamp;

    /**
     * Create an AI configuration exception with context.
     *
     * @param provider the AI provider name (e.g., "groq", "openai")
     * @param configurationKey the configuration key that failed (e.g., "api-key", "base-url")
     * @param message the technical error message
     * @param cause the underlying cause
     */
    public AIConfigurationException(String provider, String configurationKey, String message, Throwable cause) {
        super(formatTechnicalMessage(provider, configurationKey, message), cause);
        this.provider = provider;
        this.configurationKey = configurationKey;
        this.userMessage = generateUserMessage(provider, configurationKey, message);
        this.timestamp = Instant.now();
    }

    /**
     * Create an AI configuration exception with context (no cause).
     *
     * @param provider the AI provider name
     * @param configurationKey the configuration key that failed
     * @param message the technical error message
     */
    public AIConfigurationException(String provider, String configurationKey, String message) {
        this(provider, configurationKey, message, null);
    }

    private static String formatTechnicalMessage(String provider, String configurationKey, String message) {
        return String.format("[AI Configuration Error] Provider: %s, Key: %s, Details: %s", 
                           provider, configurationKey, message);
    }

    private String generateUserMessage(String provider, String configurationKey, String message) {
        // Handle null values gracefully
        String safeProvider = provider != null ? provider : "unknown";
        String safeConfigKey = configurationKey != null ? configurationKey : "unknown";
        String safeMessage = message != null ? message : "unknown error";
        
        return switch (safeConfigKey) {
            case "api-key" -> String.format("Configure a API key do %s corretamente", safeProvider);
            case "base-url" -> String.format("Verifique a URL de configuração do %s", safeProvider);
            case "connection" -> String.format("Não foi possível conectar ao %s. Verifique sua conexão", safeProvider);
            case "model" -> String.format("Modelo especificado para %s não é válido", safeProvider);
            case "creation" -> String.format("Falha ao inicializar o %s. Verifique as configurações", safeProvider);
            default -> String.format("Erro de configuração no %s: %s", safeProvider, safeMessage);
        };
    }

    // Getters
    public String getProvider() {
        return provider;
    }

    public String getConfigurationKey() {
        return configurationKey;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Get a summary of the error for logging.
     */
    public String getErrorSummary() {
        return String.format("AI Config Error - Provider: %s, Key: %s, Time: %s", 
                           provider, configurationKey, timestamp);
    }
}