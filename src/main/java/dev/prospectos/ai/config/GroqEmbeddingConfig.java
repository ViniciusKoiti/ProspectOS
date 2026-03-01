package dev.prospectos.ai.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

import static dev.prospectos.ai.config.AIConfigurationProperties.*;

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
    @ConditionalOnProperty(
        name = GROQ_ENABLED,
        havingValue = "true",
        matchIfMissing = false
    )
    @Profile(EXCLUDE_TEST_PROFILE)
    public EmbeddingModel groqEmbeddingModel() {
        // Validate API key first
        if (groqApiKey == null || groqApiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Groq API key is required but not configured");
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

            return new OpenAiEmbeddingModel(openAiApi, null, null, null);
        } catch (Exception e) {
            log.error("Failed to create Groq EmbeddingModel: {}", e.getMessage(), e);
            throw new IllegalStateException("Unable to initialize Groq EmbeddingModel", e);
        }
    }
}
