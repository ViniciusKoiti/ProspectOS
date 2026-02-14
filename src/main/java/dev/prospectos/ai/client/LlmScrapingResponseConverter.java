package dev.prospectos.ai.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts non-strict LLM text responses into a normalized scraper payload map.
 */
@Slf4j
@Component
public class LlmScrapingResponseConverter {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private static final Set<String> NULL_LIKE = Set.of(
        "none", "null", "n/a", "na", "not available", "unknown", "sem dados", "nao disponivel", "não disponível"
    );

    private final ObjectMapper objectMapper = JsonMapper.builder()
        .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
        .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
        .build();

    public Map<String, Object> convert(String response) {
        String cleaned = preprocess(response);
        String json = extractJsonFromResponse(cleaned);

        if (json == null) {
            Map<String, Object> loose = parseLooseFields(cleaned);
            if (!loose.isEmpty()) {
                return normalize(loose, cleaned);
            }
            return fallback(cleaned, null);
        }

        try {
            Map<String, Object> raw = objectMapper.readValue(json, MAP_TYPE);
            return normalize(raw, cleaned);
        } catch (JsonProcessingException ex) {
            log.debug("Failed strict JSON parse for LLM scraping response: {}", ex.getMessage());
            Map<String, Object> loose = parseLooseFields(cleaned);
            if (!loose.isEmpty()) {
                return normalize(loose, cleaned);
            }
            return fallback(cleaned, ex.getMessage());
        }
    }

    private String preprocess(String response) {
        if (response == null) {
            return "";
        }

        String value = response.trim();
        value = value.replace("```json", "").replace("```", "");
        value = removeReferencesSection(value);
        value = removeLineComments(value);
        value = value.replaceAll("\\[(\\d+)]", "");
        value = value.replaceAll("\"\\s*\\(([^)]*)\\)", " ($1)\"");
        value = value.replaceAll(",\\s*([}\\]])", "$1");
        return value.trim();
    }

    private String removeReferencesSection(String value) {
        Matcher matcher = Pattern.compile("(?is)\\n\\s*references\\s*:.*$").matcher(value);
        return matcher.find() ? value.substring(0, matcher.start()) : value;
    }

    private String removeLineComments(String value) {
        String[] lines = value.split("\\R");
        StringBuilder result = new StringBuilder();
        for (String line : lines) {
            String stripped = stripLineComment(line);
            if (!stripped.isBlank()) {
                if (!result.isEmpty()) {
                    result.append('\n');
                }
                result.append(stripped);
            }
        }
        return result.toString();
    }

    private String stripLineComment(String line) {
        boolean inString = false;
        boolean escaped = false;
        for (int i = 0; i < line.length() - 1; i++) {
            char c = line.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
                continue;
            }
            if (c == '/' && line.charAt(i + 1) == '/') {
                return line.substring(0, i).trim();
            }
        }
        return line.trim();
    }

    private String extractJsonFromResponse(String response) {
        if (response == null || response.isBlank()) {
            return null;
        }

        return extractFirstBalancedJsonObject(response);
    }

    private String extractFirstBalancedJsonObject(String value) {
        int start = value.indexOf('{');
        if (start < 0) {
            return null;
        }

        int depth = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = start; i < value.length(); i++) {
            char c = value.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }

            if (c == '"') {
                inString = true;
            } else if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return value.substring(start, i + 1);
                }
            }
        }
        return null;
    }

    private Map<String, Object> parseLooseFields(String text) {
        Map<String, Object> map = new LinkedHashMap<>();

        putScalar(text, "company_name", map);
        putScalar(text, "description", map);
        putScalar(text, "phone", map);
        putScalar(text, "industry", map);
        putScalar(text, "size", map);
        putArray(text, "emails", map);
        putArray(text, "technologies", map);
        putArray(text, "recent_news", map);

        return map;
    }

    private void putScalar(String text, String key, Map<String, Object> map) {
        Matcher matcher = Pattern.compile("\"" + key + "\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL).matcher(text);
        if (matcher.find()) {
            map.put(key, matcher.group(1).trim());
        }
    }

    private void putArray(String text, String key, Map<String, Object> map) {
        Matcher matcher = Pattern.compile("\"" + key + "\"\\s*:\\s*\\[(.*?)]", Pattern.DOTALL).matcher(text);
        if (!matcher.find()) {
            return;
        }
        String raw = matcher.group(1);
        Matcher quoted = Pattern.compile("\"(.*?)\"", Pattern.DOTALL).matcher(raw);
        List<String> values = new ArrayList<>();
        while (quoted.find()) {
            String value = quoted.group(1).trim();
            if (!value.isBlank()) {
                values.add(value);
            }
        }
        if (!values.isEmpty()) {
            map.put(key, values);
        }
    }

    private Map<String, Object> normalize(Map<String, Object> raw, String originalResponse) {
        Map<String, Object> normalized = new LinkedHashMap<>();

        normalized.put("company_name", normalizeScalar(raw.get("company_name")));
        normalized.put("description", normalizeScalar(raw.get("description")));
        normalized.put("emails", normalizeStringList(raw.get("emails")));
        normalized.put("phone", normalizeScalar(raw.get("phone")));
        normalized.put("technologies", normalizeStringList(raw.get("technologies")));
        normalized.put("industry", normalizeScalar(raw.get("industry")));
        normalized.put("size", normalizeScalar(raw.get("size")));
        normalized.put("recent_news", normalizeStringList(raw.get("recent_news")));
        normalized.put("source", "ai_web_search");
        normalized.put("ai_processed", true);

        if (normalized.get("description") == null && originalResponse != null && !originalResponse.isBlank()) {
            normalized.put("description", originalResponse.trim());
        }
        return normalized;
    }

    private String normalizeScalar(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        if (text.isBlank()) {
            return null;
        }
        String lowered = text.toLowerCase(Locale.ROOT);
        return NULL_LIKE.contains(lowered) ? null : text;
    }

    private List<String> normalizeStringList(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                .map(this::normalizeScalar)
                .filter(v -> v != null && !v.isBlank())
                .toList();
        }
        String scalar = normalizeScalar(value);
        return scalar == null ? List.of() : List.of(scalar);
    }

    private Map<String, Object> fallback(String response, String parseError) {
        Map<String, Object> fallback = new LinkedHashMap<>();
        fallback.put("description", response == null ? "" : response.trim());
        fallback.put("source", "ai_web_search");
        fallback.put("ai_processed", true);
        if (parseError != null) {
            fallback.put("parse_error", parseError);
        }
        return fallback;
    }
}
