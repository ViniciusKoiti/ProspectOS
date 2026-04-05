package dev.prospectos.ai.factory;

import dev.prospectos.ai.client.LLMProvider;
import dev.prospectos.ai.config.AIProviderCredentials;

final class LLMProviderAvailabilityChecker {

    private final AIProviderCredentials credentials;

    LLMProviderAvailabilityChecker(AIProviderCredentials credentials) {
        this.credentials = credentials;
    }

    boolean isProviderAvailable(LLMProvider provider) {
        return switch (provider) {
            case OPENAI -> credentials.hasValidOpenAiKey();
            case ANTHROPIC -> credentials.hasValidAnthropicKey();
            case GROQ -> credentials.hasValidGroqKey();
            case OLLAMA -> false;
            case MOCK -> true;
        };
    }
}
