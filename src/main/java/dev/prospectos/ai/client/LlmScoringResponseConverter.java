package dev.prospectos.ai.client;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.stereotype.Component;

import dev.prospectos.ai.dto.ScoringResult;
import lombok.extern.slf4j.Slf4j;

/**
 * Converts raw LLM scoring text into a validated ScoringResult.
 */
@Slf4j
@Component
public class LlmScoringResponseConverter {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final LlmStructuredResponseSanitizer sanitizer;
    private final ObjectMapper objectMapper = JsonMapper.builder()
        .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
        .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
        .build();
    private final LlmScoringValueParser valueParser = new LlmScoringValueParser();
    private final LlmScoringBreakdownNormalizer breakdownNormalizer = new LlmScoringBreakdownNormalizer(valueParser);
    private final LlmScoringTextFallbackParser fallbackParser =
        new LlmScoringTextFallbackParser(valueParser, breakdownNormalizer);

    public LlmScoringResponseConverter(LlmStructuredResponseSanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }

    public ScoringResult convert(String rawResponse) {
        String preprocessed = sanitizer.preprocess(rawResponse);
        String json = sanitizer.extractFirstJsonObject(preprocessed);
        if (json == null) {
            return fallbackParser.parse(preprocessed);
        }

        String sanitizedJson = sanitizer.sanitizeJson(json);
        try {
            Map<String, Object> map = objectMapper.readValue(sanitizedJson, MAP_TYPE);
            return toScoringResult(map, preprocessed);
        } catch (JsonProcessingException ex) {
            log.warn("Could not parse scoring JSON directly, using fallback converter. Error: {}", ex.getMessage());
            return fallbackParser.parse(preprocessed);
        }
    }

    private ScoringResult toScoringResult(Map<String, Object> map, String originalText) {
        int score = valueParser.clamp(valueParser.parseInt(map.get("score"), 0), 0, 100);
        var priority = valueParser.parsePriority(map.get("priority"));
        String reasoning = valueParser.normalizeText(map.get("reasoning"), "Unable to parse AI reasoning");
        String recommendation = valueParser.normalizeText(map.get("recommendation"), "No recommendation available.");
        Map<String, Integer> breakdown = breakdownNormalizer.normalize(map.get("breakdown"));

        if (score == 0 && originalText != null && !originalText.isBlank()) {
            score = valueParser.clamp(fallbackParser.extractScore(originalText), 0, 100);
        }
        return new ScoringResult(score, priority, reasoning, breakdown, recommendation);
    }
}
