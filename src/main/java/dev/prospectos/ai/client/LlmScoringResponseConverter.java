package dev.prospectos.ai.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import dev.prospectos.ai.dto.PriorityLevel;
import dev.prospectos.ai.dto.ScoringResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public LlmScoringResponseConverter(LlmStructuredResponseSanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }

    public ScoringResult convert(String rawResponse) {
        String preprocessed = sanitizer.preprocess(rawResponse);
        String json = sanitizer.extractFirstJsonObject(preprocessed);

        if (json == null) {
            return fallbackFromText(preprocessed);
        }

        String sanitizedJson = sanitizer.sanitizeJson(json);
        try {
            Map<String, Object> map = objectMapper.readValue(sanitizedJson, MAP_TYPE);
            return toScoringResult(map, preprocessed);
        } catch (JsonProcessingException ex) {
            log.warn("Could not parse scoring JSON directly, using fallback converter. Error: {}", ex.getMessage());
            return fallbackFromText(preprocessed);
        }
    }

    private ScoringResult toScoringResult(Map<String, Object> map, String originalText) {
        int score = clamp(parseInt(map.get("score"), 0), 0, 100);
        PriorityLevel priority = parsePriority(map.get("priority"));
        String reasoning = normalizeText(map.get("reasoning"), "Unable to parse AI reasoning");
        String recommendation = normalizeText(map.get("recommendation"), "No recommendation available.");
        Map<String, Integer> breakdown = normalizeBreakdown(map.get("breakdown"));

        if (score == 0 && originalText != null && !originalText.isBlank()) {
            int extracted = extractInt(originalText, "\"score\"\\s*:\\s*(\\d+)");
            score = clamp(extracted, 0, 100);
        }

        return new ScoringResult(score, priority, reasoning, breakdown, recommendation);
    }

    private Map<String, Integer> normalizeBreakdown(Object rawBreakdown) {
        Map<String, Integer> normalized = new LinkedHashMap<>();
        normalized.put("icpFit", 0);
        normalized.put("signals", 0);
        normalized.put("companySize", 0);
        normalized.put("timing", 0);
        normalized.put("accessibility", 0);

        if (!(rawBreakdown instanceof Map<?, ?> breakdownMap)) {
            return normalized;
        }

        for (Map.Entry<?, ?> entry : breakdownMap.entrySet()) {
            String key = canonicalBreakdownKey(String.valueOf(entry.getKey()));
            if (key == null) {
                continue;
            }
            int max = maxForKey(key);
            int value = clamp(parseInt(entry.getValue(), 0), 0, max);
            normalized.put(key, value);
        }
        return normalized;
    }

    private String canonicalBreakdownKey(String key) {
        if (key == null) {
            return null;
        }
        String normalized = key.toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]", "");

        return switch (normalized) {
            case "icpfit" -> "icpFit";
            case "interestsignals", "signals" -> "signals";
            case "companysizeandmaturity", "companysize" -> "companySize";
            case "timingandurgency", "timing" -> "timing";
            case "contactaccessibility", "accessibility" -> "accessibility";
            default -> null;
        };
    }

    private int maxForKey(String key) {
        return switch (key) {
            case "icpFit" -> 30;
            case "signals" -> 25;
            case "companySize" -> 20;
            case "timing" -> 15;
            case "accessibility" -> 10;
            default -> 100;
        };
    }

    private PriorityLevel parsePriority(Object raw) {
        if (raw == null) {
            return PriorityLevel.COLD;
        }
        try {
            return PriorityLevel.valueOf(String.valueOf(raw).trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return PriorityLevel.COLD;
        }
    }

    private String normalizeText(Object raw, String fallback) {
        String value = raw == null ? "" : String.valueOf(raw);
        value = value.replace('\r', ' ').replace('\n', ' ').replace('\t', ' ').trim();
        return value.isBlank() ? fallback : value;
    }

    private int parseInt(Object value, int fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private ScoringResult fallbackFromText(String text) {
        int score = clamp(extractInt(text, "\"score\"\\s*:\\s*(\\d+)"), 0, 100);
        PriorityLevel priority = parsePriority(extractText(text, "\"priority\"\\s*:\\s*\"([^\"]+)\""));
        String reasoning = normalizeText(extractText(text, "\"reasoning\"\\s*:\\s*\"([^\"]+)\""), "Unable to parse AI reasoning");
        String recommendation = normalizeText(
            extractText(text, "\"recommendation\"\\s*:\\s*\"([^\"]+)\""),
            "No recommendation available."
        );

        Map<String, Integer> breakdown = new LinkedHashMap<>();
        breakdown.put("icpFit", 0);
        breakdown.put("signals", 0);
        breakdown.put("companySize", 0);
        breakdown.put("timing", 0);
        breakdown.put("accessibility", 0);

        return new ScoringResult(score, priority, reasoning, breakdown, recommendation);
    }

    private int extractInt(String text, String regex) {
        if (text == null) {
            return 0;
        }
        Matcher matcher = Pattern.compile(regex).matcher(text);
        if (!matcher.find()) {
            return 0;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String extractText(String text, String regex) {
        if (text == null) {
            return null;
        }
        Matcher matcher = Pattern.compile(regex, Pattern.DOTALL).matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
