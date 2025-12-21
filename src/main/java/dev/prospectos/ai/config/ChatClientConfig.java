package dev.prospectos.ai.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ChatClientConfig {
    
    @Value("${spring.ai.openai.api-key:}")
    private String openaiKey;
    
    @Value("${spring.ai.anthropic.api-key:}")
    private String anthropicKey;
    
    /**
     * Automatically selects the best available ChatModel.
     */
    @Bean
    @Primary
    public ChatModel primaryChatModel(
        @Autowired(required = false) OpenAiChatModel openAiChatModel
    ) {
        if (!openaiKey.isBlank() && openAiChatModel != null) {
            return openAiChatModel;
        }
        
        throw new IllegalStateException(
            "No LLM API key configured. Set OPENAI_API_KEY or ANTHROPIC_API_KEY"
        );
    }
}
