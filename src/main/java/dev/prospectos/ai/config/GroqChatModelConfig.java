package dev.prospectos.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
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

import static dev.prospectos.ai.config.AIConfigurationProperties.*;

@Configuration
@Slf4j
public class GroqChatModelConfig {

    @Value("${" + GROQ_API_KEY + ":}")
    private String groqApiKey;

    @Value("${" + GROQ_BASE_URL + ":" + DEFAULT_GROQ_BASE_URL + "}")
    private String groqBaseUrl;

    @Value("${" + GROQ_MODEL + ":" + DEFAULT_GROQ_MODEL + "}")
    private String groqModel;

    @Bean("groqChatModel")
    @ConditionalOnProperty(
        name = GROQ_ENABLED,
        havingValue = "true",
        matchIfMissing = false
    )
    @Profile(EXCLUDE_TEST_PROFILE)
    public ChatModel groqChatModel() {
        // Validate API key first
        if (groqApiKey == null || groqApiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Groq API key is required but not configured");
        }

        String normalizedBaseUrl = normalizeBaseUrl(groqBaseUrl);
        log.info("Groq base URL: {}", normalizedBaseUrl);

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

            OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(groqModel)
                .build();

            return new OpenAiChatModel(openAiApi, options, null, null, null);
        } catch (Exception e) {
            log.error("Failed to create Groq ChatModel: {}", e.getMessage(), e);
            throw new IllegalStateException("Unable to initialize Groq ChatModel", e);
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "https://api.groq.com/openai/v1";
        }

        String normalized = baseUrl.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (!normalized.endsWith("/v1")) {
            normalized = normalized + "/v1";
        }
        return normalized;
    }
}
