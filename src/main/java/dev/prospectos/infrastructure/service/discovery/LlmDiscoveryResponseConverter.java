package dev.prospectos.infrastructure.service.discovery;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.prospectos.ai.client.LlmStructuredResponseSanitizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Converts LLM discovery JSON into normalized lead candidates.
 */
@Component
@Slf4j
public class LlmDiscoveryResponseConverter {

    private final LlmStructuredResponseSanitizer sanitizer;
    private final ObjectMapper objectMapper;
    private final ConcurrentMap<String, AtomicLong> parseFailuresBySource;

    public LlmDiscoveryResponseConverter(LlmStructuredResponseSanitizer sanitizer) {
        this.sanitizer = sanitizer;
        this.objectMapper = new ObjectMapper();
        this.parseFailuresBySource = new ConcurrentHashMap<>();
    }

    public List<DiscoveredLeadCandidate> convert(String rawResponse, String sourceName) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return List.of();
        }

        String preprocessed = sanitizer.preprocess(rawResponse);
        String json = sanitizer.extractFirstJsonObject(preprocessed);
        if (json == null) {
            throw parseFailure(sourceName, "No JSON object found in LLM response", null);
        }

        String sanitizedJson = sanitizer.sanitizeJson(json);
        try {
            DiscoveryResponse response = objectMapper.readValue(sanitizedJson, DiscoveryResponse.class);
            if (response == null || response.candidates() == null) {
                throw parseFailure(sourceName, "Missing required field 'candidates' in LLM response", null);
            }

            List<DiscoveredLeadCandidate> candidates = new ArrayList<>();
            for (DiscoveryCandidate item : response.candidates()) {
                if (item == null) {
                    continue;
                }
                String name = asText(item.name());
                String website = normalizeWebsite(asText(item.website()));
                if (name == null || website == null) {
                    continue;
                }
                String industry = defaultIfBlank(asText(item.industry()), "Other");
                String description = defaultIfBlank(asText(item.description()), "");
                String location = asText(item.location());
                List<String> contacts = asStringList(item.contacts());

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
        } catch (JsonProcessingException ex) {
            throw parseFailure(sourceName, "Malformed JSON in LLM response", ex);
        }
    }

    long parseFailureCount(String sourceName) {
        String sourceKey = sourceName == null || sourceName.isBlank() ? "unknown" : sourceName;
        return parseFailuresBySource.getOrDefault(sourceKey, new AtomicLong(0)).get();
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

    private List<String> asStringList(List<String> value) {
        if (value == null || value.isEmpty()) {
            return List.of();
        }
        return value.stream()
            .map(this::asText)
            .filter(v -> v != null && !v.isBlank())
            .toList();
    }

    private IllegalArgumentException parseFailure(String sourceName, String message, Exception cause) {
        String sourceKey = sourceName == null || sourceName.isBlank() ? "unknown" : sourceName;
        long count = parseFailuresBySource.computeIfAbsent(sourceKey, ignored -> new AtomicLong(0))
            .incrementAndGet();

        if (cause == null) {
            log.warn("Discovery parse failure. source={} reason={} parseFailureCount={}", sourceKey, message, count);
            return new IllegalArgumentException(message);
        }

        log.warn("Discovery parse failure. source={} reason={} parseFailureCount={}", sourceKey, message, count, cause);
        return new IllegalArgumentException(message, cause);
    }

    private record DiscoveryResponse(List<DiscoveryCandidate> candidates) {
    }

    private record DiscoveryCandidate(
        String name,
        String website,
        String industry,
        String description,
        String location,
        List<String> contacts
    ) {
    }
}
