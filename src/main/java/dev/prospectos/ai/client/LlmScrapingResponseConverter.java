package dev.prospectos.ai.client;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LlmScrapingResponseConverter {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper = JsonMapper.builder()
        .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
        .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
        .build();
    private final LlmScrapingResponsePreprocessor preprocessor = new LlmScrapingResponsePreprocessor();
    private final LlmScrapingLooseFieldParser looseFieldParser = new LlmScrapingLooseFieldParser();
    private final LlmScrapingPayloadNormalizer payloadNormalizer = new LlmScrapingPayloadNormalizer();

    public Map<String, Object> convert(String response) {
        String cleaned = preprocessor.preprocess(response);
        String json = preprocessor.extractJsonFromResponse(cleaned);
        if (json == null) {
            return normalizeLooseOrFallback(cleaned, null);
        }
        try {
            Map<String, Object> raw = objectMapper.readValue(json, MAP_TYPE);
            return payloadNormalizer.normalize(raw, cleaned);
        } catch (JsonProcessingException ex) {
            log.debug("Failed strict JSON parse for LLM scraping response: {}", ex.getMessage());
            return normalizeLooseOrFallback(cleaned, ex.getMessage());
        }
    }

    private Map<String, Object> normalizeLooseOrFallback(String cleaned, String parseError) {
        Map<String, Object> loose = looseFieldParser.parseLooseFields(cleaned);
        if (!loose.isEmpty()) {
            return payloadNormalizer.normalize(loose, cleaned);
        }
        return payloadNormalizer.fallback(cleaned, parseError);
    }
}
