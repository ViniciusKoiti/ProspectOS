package dev.prospectos.ai.factory;

import java.util.function.Supplier;

import dev.prospectos.ai.client.LLMProvider;

final class LLMProviderAvailabilityChecker {

    private final Supplier<String> openaiKeySupplier;
    private final Supplier<String> anthropicKeySupplier;
    private final Supplier<String> groqKeySupplier;

    LLMProviderAvailabilityChecker(
        Supplier<String> openaiKeySupplier,
        Supplier<String> anthropicKeySupplier,
        Supplier<String> groqKeySupplier
    ) {
        this.openaiKeySupplier = openaiKeySupplier;
        this.anthropicKeySupplier = anthropicKeySupplier;
        this.groqKeySupplier = groqKeySupplier;
    }

    boolean isProviderAvailable(LLMProvider provider) {
        return switch (provider) {
            case OPENAI -> isValidApiKey(openaiKeySupplier.get());
            case ANTHROPIC -> isValidApiKey(anthropicKeySupplier.get());
            case GROQ -> isValidApiKey(groqKeySupplier.get());
            case OLLAMA -> false;
            case MOCK -> true;
        };
    }

    private boolean isValidApiKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }
        String trimmedKey = key.trim();
        return !trimmedKey.equals("test-key")
            && !trimmedKey.equals("dummy-key")
            && !trimmedKey.equals("fake-key")
            && !trimmedKey.equals("mock-key")
            && !trimmedKey.startsWith("sk-test-")
            && !trimmedKey.matches("(?i)test.*|mock.*|fake.*|dummy.*|dev.*");
    }
}
