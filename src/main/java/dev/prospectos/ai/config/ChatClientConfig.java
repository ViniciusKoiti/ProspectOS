package dev.prospectos.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.Optional;

@Configuration
@Profile("!mock & !test")
@Slf4j
public class ChatClientConfig {
    
    @Value("${spring.ai.openai.api-key:}")
    private String openaiKey;
    
    @Value("${spring.ai.anthropic.api-key:}")
    private String anthropicKey;

    @Value("${prospectos.ai.groq.api-key:}")
    private String groqKey;
    
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
        if (isValidApiKey(openaiKey) && openAiChatModel != null) {
            log.info("Using OpenAI ChatModel as primary.");
            return openAiChatModel;
        }

        if (isValidApiKey(groqKey) && groqChatModel != null) {
            log.info("Using Groq ChatModel as primary.");
            return groqChatModel;
        }

        if (isValidApiKey(anthropicKey) && anthropicChatModel != null) {
            log.info("Using Anthropic ChatModel as primary.");
            return anthropicChatModel;
        }
        
        throw new IllegalStateException(
            "No LLM API key configured. Set OPENAI_API_KEY, ANTHROPIC_API_KEY, or GROQ_API_KEY"
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
