package dev.prospectos.infrastructure.service.discovery;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.prospectos.ai.client.LlmStructuredResponseSanitizer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Converts LLM discovery JSON into normalized lead candidates.
 */
@Component
public class LlmDiscoveryResponseConverter {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final LlmStructuredResponseSanitizer sanitizer;
    private final ObjectMapper objectMapper;

    public LlmDiscoveryResponseConverter(LlmStructuredResponseSanitizer sanitizer) {
        this.sanitizer = sanitizer;
        this.objectMapper = new ObjectMapper();
    }

    public List<DiscoveredLeadCandidate> convert(String rawResponse, String sourceName) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return List.of();
        }

        String preprocessed = sanitizer.preprocess(rawResponse);
        String json = sanitizer.extractFirstJsonObject(preprocessed);
        if (json == null) {
            return List.of();
        }

        String sanitizedJson = sanitizer.sanitizeJson(json);
        try {
            Map<String, Object> root = objectMapper.readValue(sanitizedJson, MAP_TYPE);
            Object candidatesRaw = root.get("candidates");
            if (!(candidatesRaw instanceof List<?> candidatesList)) {
                return List.of();
            }

            List<DiscoveredLeadCandidate> candidates = new ArrayList<>();
            for (Object item : candidatesList) {
                if (!(item instanceof Map<?, ?> map)) {
                    continue;
                }
                String name = asText(map.get("name"));
                String website = normalizeWebsite(asText(map.get("website")));
                if (name == null || website == null) {
                    continue;
                }
                String industry = defaultIfBlank(asText(map.get("industry")), "Other");
                String description = defaultIfBlank(asText(map.get("description")), "");
                String location = asText(map.get("location"));
                List<String> contacts = asStringList(map.get("contacts"));

                candidates.add(new DiscoveredLeadCandidate(
                    name,
                    website,
                    industry,
                    description,
                    location,
                    contacts,
                    sourceName
                ));
            }

            return candidates;
        } catch (JsonProcessingException ignored) {
            return List.of();
        }
    }

    private String normalizeWebsite(String website) {
        if (website == null || website.isBlank()) {
            return null;
        }
        String trimmed = website.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        if (trimmed.contains(" ")) {
            return null;
        }
        return "https://" + trimmed;
    }

    private String asText(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isBlank() ? null : text;
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private List<String> asStringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream()
            .map(this::asText)
            .filter(v -> v != null && !v.isBlank())
            .toList();
    }
}
