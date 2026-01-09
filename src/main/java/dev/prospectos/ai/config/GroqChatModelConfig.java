package dev.prospectos.ai.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GroqChatModelConfig {

    @Value("${prospectos.ai.groq.api-key:}")
    private String groqApiKey;

    @Value("${prospectos.ai.groq.base-url:https://api.groq.com/openai/v1}")
    private String groqBaseUrl;

    @Value("${prospectos.ai.groq.model:llama3-70b-8192}")
    private String groqModel;

    @Bean("groqChatModel")
    @ConditionalOnProperty(name = "prospectos.ai.groq.api-key")
    public ChatModel groqChatModel() {
        OpenAiApi openAiApi = new OpenAiApi(groqApiKey, groqBaseUrl);
        OpenAiChatOptions options = OpenAiChatOptions.builder()
            .withModel(groqModel)
            .build();
        return new OpenAiChatModel(openAiApi, options);
    }
}
