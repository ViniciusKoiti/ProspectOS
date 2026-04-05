package dev.prospectos.ai.config;

import dev.prospectos.ai.client.LLMProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
@Slf4j
public class ChatClientConfig {

    private final AIProviderActivationProperties activationProperties;
    private final AIProviderCredentials credentials;

    public ChatClientConfig(AIProviderActivationProperties activationProperties, AIProviderCredentials credentials) {
        this.activationProperties = activationProperties;
        this.credentials = credentials;
    }
    
    /**
     * Automatically selects the best available ChatModel.
     */
    @Bean
    @Primary
    public ChatModel primaryChatModel(
        @Qualifier("openAiChatModel") @Autowired(required = false) ChatModel openAiChatModel,
        @Qualifier("groqChatModel") @Autowired(required = false) ChatModel groqChatModel,
        @Qualifier("anthropicChatModel") @Autowired(required = false) ChatModel anthropicChatModel
    ) {
        log.info("Selecting primary ChatModel. OpenAI key set: {}, Groq key set: {}, Anthropic key set: {}",
            credentials.hasValidOpenAiKey(),
            credentials.hasValidGroqKey(),
            credentials.hasValidAnthropicKey());
        log.info("ChatModel beans present. OpenAI: {}, Groq: {}, Anthropic: {}",
            openAiChatModel != null,
            groqChatModel != null,
            anthropicChatModel != null);
        log.info("Configured active providers order: {}", activationProperties.activeProviders());

        for (LLMProvider provider : activationProperties.activeProviders()) {
            boolean keyValid = switch (provider) {
                case OPENAI -> credentials.hasValidOpenAiKey();
                case GROQ -> credentials.hasValidGroqKey();
                case ANTHROPIC -> credentials.hasValidAnthropicKey();
                default -> false;
            };
            ChatModel candidate = switch (provider) {
                case OPENAI -> keyValid ? openAiChatModel : null;
                case GROQ -> keyValid ? groqChatModel : null;
                case ANTHROPIC -> keyValid ? anthropicChatModel : null;
                default -> null;
            };
            log.info("Provider {} evaluation -> keyValid={}, modelPresent={}", provider, keyValid, candidate != null);

            if (candidate != null) {
                log.info("Using {} ChatModel as primary.", provider.getDisplayName());
                return candidate;
            }
        }
        
        throw new IllegalStateException(
            "No active LLM provider is available. Check "
                + AIConfigurationProperties.ACTIVE_PROVIDERS
                + ", provider API keys, and Spring AI provider bootstrap flags."
        );
    }
}
