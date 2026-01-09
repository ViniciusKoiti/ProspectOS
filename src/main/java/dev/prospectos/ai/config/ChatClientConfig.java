package dev.prospectos.ai.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.Optional;

@Configuration
@Profile("!mock")
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
        @Qualifier("openAiChatModel") Optional<ChatModel> openAiChatModel,
        @Qualifier("groqChatModel") Optional<ChatModel> groqChatModel,
        @Qualifier("anthropicChatModel") Optional<ChatModel> anthropicChatModel
    ) {
        if (isValidApiKey(openaiKey) && openAiChatModel.isPresent()) {
            return openAiChatModel.get();
        }

        if (isValidApiKey(groqKey) && groqChatModel.isPresent()) {
            return groqChatModel.get();
        }

        if (isValidApiKey(anthropicKey) && anthropicChatModel.isPresent()) {
            return anthropicChatModel.get();
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
