package dev.prospectos.ai.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class GroqEmbeddingConfig {
    
    @Value("${prospectos.ai.groq.api-key:}")
    private String groqApiKey;
    
    @Value("${prospectos.ai.groq.base-url:https://api.groq.com/openai}")
    private String groqBaseUrl;
    
    @Bean("groqEmbeddingModel")
    @ConditionalOnProperty(name = "prospectos.ai.groq.api-key")
    public EmbeddingModel groqEmbeddingModel() {
        String normalizedBaseUrl = normalizeBaseUrl(groqBaseUrl);
        log.info("Creating Groq EmbeddingModel with base URL: {}", normalizedBaseUrl);
        
        OpenAiApi openAiApi = new OpenAiApi(
            normalizedBaseUrl,
            new SimpleApiKey(groqApiKey),
            new LinkedMultiValueMap<>(), // headers
            null,     // userAgent
            null,     // threadExecutorServiceName
            RestClient.builder(),
            null,     // WebClient.Builder  
            null      // ResponseErrorHandler
        );
        
        return new OpenAiEmbeddingModel(openAiApi, null, null, null);
    }
    
    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return "https://api.groq.com/openai";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}