package dev.prospectos.ai.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

import static dev.prospectos.ai.config.AIConfigurationProperties.*;
import dev.prospectos.ai.exception.AIConfigurationException;

@Configuration
@Slf4j
public class GroqEmbeddingConfig {

    private final UrlNormalizationService urlNormalizationService;

    @Value("${" + GROQ_API_KEY + ":}")
    private String groqApiKey;

    @Value("${" + GROQ_BASE_URL + ":" + DEFAULT_GROQ_BASE_URL + "}")
    private String groqBaseUrl;

    public GroqEmbeddingConfig(UrlNormalizationService urlNormalizationService) {
        this.urlNormalizationService = urlNormalizationService;
    }

    @Bean("groqEmbeddingModel")
    @ConditionalOnActiveAIProvider(dev.prospectos.ai.client.LLMProvider.GROQ)
    @Profile(EXCLUDE_TEST_PROFILE)
    public EmbeddingModel groqEmbeddingModel() {
        // Validate API key first
        if (groqApiKey == null || groqApiKey.trim().isEmpty()) {
            log.error("Groq API key is missing or empty");
            throw new AIConfigurationException("groq", "api-key", "API key is required but not configured");
        }

        String normalizedBaseUrl = urlNormalizationService.normalizeGroqUrl(groqBaseUrl);
        log.info("Creating Groq EmbeddingModel with base URL: {}", normalizedBaseUrl);

        try {
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

            log.info("✅ Groq EmbeddingModel created successfully");
            return new OpenAiEmbeddingModel(openAiApi, null, null, null);
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid Groq configuration: {}", e.getMessage());
            throw new AIConfigurationException("groq", "api-key", "Invalid API key format", e);
            
        } catch (org.springframework.web.client.RestClientException e) {
            log.error("❌ Groq API connection failed: {}", e.getMessage());
            throw new AIConfigurationException("groq", "connection", "Failed to connect to Groq API", e);
            
        } catch (Exception e) {
            log.error("❌ Unexpected error creating Groq EmbeddingModel: {}", e.getMessage(), e);
            throw new AIConfigurationException("groq", "creation", "Unexpected initialization error", e);
        }
    }
}
