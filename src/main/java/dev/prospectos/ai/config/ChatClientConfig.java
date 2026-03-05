package dev.prospectos.ai.config;

import dev.prospectos.ai.client.LLMProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
@Slf4j
public class ChatClientConfig {

    private final AIProviderActivationProperties activationProperties;
    
    @Value("${spring.ai.openai.api-key:}")
    private String openaiKey;
    
    @Value("${spring.ai.anthropic.api-key:}")
    private String anthropicKey;

    @Value("${prospectos.ai.groq.api-key:}")
    private String groqKey;

    public ChatClientConfig(AIProviderActivationProperties activationProperties) {
        this.activationProperties = activationProperties;
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
            isValidApiKey(openaiKey),
            isValidApiKey(groqKey),
            isValidApiKey(anthropicKey));
        log.info("ChatModel beans present. OpenAI: {}, Groq: {}, Anthropic: {}",
            openAiChatModel != null,
            groqChatModel != null,
            anthropicChatModel != null);

        for (LLMProvider provider : activationProperties.activeProviders()) {
            ChatModel candidate = switch (provider) {
                case OPENAI -> isValidApiKey(openaiKey) ? openAiChatModel : null;
                case GROQ -> isValidApiKey(groqKey) ? groqChatModel : null;
                case ANTHROPIC -> isValidApiKey(anthropicKey) ? anthropicChatModel : null;
                default -> null;
            };

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
