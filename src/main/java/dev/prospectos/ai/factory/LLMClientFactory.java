package dev.prospectos.ai.factory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import dev.prospectos.ai.client.LLMClient;
import dev.prospectos.ai.client.LLMProvider;
import dev.prospectos.ai.config.AIProviderActivationProperties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LLMClientFactory {
    @Value("${spring.ai.openai.api-key:}")
    private String openaiKey;
    @Value("${spring.ai.anthropic.api-key:}")
    private String anthropicKey;
    @Value("${prospectos.ai.groq.api-key:}")
    private String groqKey;
    private final Environment environment;
    private final AIProviderActivationProperties activationProperties;
    private final LLMProviderAvailabilityChecker availabilityChecker;
    private final LLMClientCreator clientCreator;

    public LLMClientFactory(
        @Qualifier("chatClient") ObjectProvider<ChatClient> chatClient,
        @Qualifier("scoringChatClient") ObjectProvider<ChatClient> scoringChatClient,
        @Qualifier("groqChatClient") ObjectProvider<ChatClient> groqChatClient,
        @Qualifier("groqScoringChatClient") ObjectProvider<ChatClient> groqScoringChatClient,
        Environment environment,
        AIProviderActivationProperties activationProperties
    ) {
        this.environment = environment;
        this.activationProperties = activationProperties;
        this.availabilityChecker =
            new LLMProviderAvailabilityChecker(() -> openaiKey, () -> anthropicKey, () -> groqKey);
        this.clientCreator = new LLMClientCreator(
            chatClient,
            scoringChatClient,
            groqChatClient,
            groqScoringChatClient,
            availabilityChecker
        );
    }

    public LLMClient createPrimaryClient() {
        return isTestEnvironment() ? clientCreator.createMockClient() : createBestAvailableClient();
    }

    public LLMClient createScoringClient() {
        return isTestEnvironment() ? clientCreator.createMockClient() : createBestAvailableScoringClient();
    }

    public LLMClient createClient(LLMProvider provider) {
        if (isTestEnvironment() && provider != LLMProvider.MOCK) {
            return clientCreator.createMockClient(provider);
        }
        return clientCreator.createProviderClient(provider, false);
    }

    public LLMClient createBestAvailableClient() {
        return selectBestAvailable(false);
    }

    private LLMClient createBestAvailableScoringClient() {
        return selectBestAvailable(true);
    }

    private LLMClient selectBestAvailable(boolean scoring) {
        if (isTestEnvironment()) {
            log.info("Test profile detected. Using Mock provider{}.", scoring ? " for scoring" : "");
            return clientCreator.createMockClient();
        }
        for (LLMProvider provider : activationProperties.activeProviders()) {
            if (provider == LLMProvider.MOCK || !availabilityChecker.isProviderAvailable(provider)) {
                continue;
            }
            LLMClient client = clientCreator.createProviderClient(provider, scoring);
            if (client != null) {
                log.info("Using {} as {} provider", provider.getDisplayName(), scoring ? "scoring" : "primary");
                return client;
            }
        }
        log.warn("No {}LLM provider configured. Using Mock for testing.", scoring ? "scoring " : "");
        return clientCreator.createMockClient();
    }

    private boolean isTestEnvironment() {
        return LLMFactoryEnvironmentDetector.isTestEnvironment(environment);
    }
}
