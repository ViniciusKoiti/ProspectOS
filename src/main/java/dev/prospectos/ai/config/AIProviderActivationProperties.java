package dev.prospectos.ai.config;

import dev.prospectos.ai.client.LLMProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Centralizes which application-level AI providers are allowed to participate
 * in provider selection and custom bean activation.
 */
@ConfigurationProperties(prefix = "prospectos.ai")
public final class AIProviderActivationProperties {

    private final List<LLMProvider> activeProviders;

    public AIProviderActivationProperties(
        @DefaultValue(AIConfigurationProperties.DEFAULT_ACTIVE_PROVIDERS) String activeProviders
    ) {
        this.activeProviders = parse(activeProviders);
    }

    public List<LLMProvider> activeProviders() {
        return activeProviders;
    }

    public boolean isActive(LLMProvider provider) {
        return activeProviders.contains(provider);
    }

    static List<LLMProvider> parse(String configuredProviders) {
        String raw = configuredProviders == null || configuredProviders.isBlank()
            ? AIConfigurationProperties.DEFAULT_ACTIVE_PROVIDERS
            : configuredProviders;

        return Arrays.stream(raw.split(","))
            .map(String::trim)
            .filter(token -> !token.isEmpty())
            .map(token -> toProvider(token, raw))
            .distinct()
            .toList();
    }

    private static LLMProvider toProvider(String token, String raw) {
        try {
            return LLMProvider.valueOf(token.toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                "Invalid provider in " + AIConfigurationProperties.ACTIVE_PROVIDERS + ": '" + token
                    + "'. Configured value: '" + raw + "'. Allowed values: openai, anthropic, groq, ollama, mock",
                exception
            );
        }
    }
}
