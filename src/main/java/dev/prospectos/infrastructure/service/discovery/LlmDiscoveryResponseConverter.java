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

@Component
@Slf4j
public class LlmDiscoveryResponseConverter {

    private final LlmStructuredResponseSanitizer sanitizer;
    private final ObjectMapper objectMapper;
    private final DiscoveryCandidateMapper mapper;
    private final ConcurrentMap<String, AtomicLong> parseFailuresBySource;

    public LlmDiscoveryResponseConverter(LlmStructuredResponseSanitizer sanitizer) {
        this.sanitizer = sanitizer;
        this.objectMapper = new ObjectMapper();
        this.mapper = new DiscoveryCandidateMapper();
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
        try {
            DiscoveryResponse response = objectMapper.readValue(sanitizer.sanitizeJson(json), DiscoveryResponse.class);
            if (response == null || response.candidates() == null) {
                throw parseFailure(sourceName, "Missing required field 'candidates' in LLM response", null);
            }
            List<DiscoveredLeadCandidate> candidates = new ArrayList<>();
            for (DiscoveryCandidate item : response.candidates()) {
                if (item == null) {
                    continue;
                }
                DiscoveredLeadCandidate candidate = mapper.toCandidate(
                    sourceName,
                    item.name(),
                    item.website(),
                    item.industry(),
                    item.description(),
                    item.location(),
                    item.contacts()
                );
                if (candidate != null) {
                    candidates.add(candidate);
                }
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

    private IllegalArgumentException parseFailure(String sourceName, String message, Exception cause) {
        String sourceKey = sourceName == null || sourceName.isBlank() ? "unknown" : sourceName;
        long count = parseFailuresBySource.computeIfAbsent(sourceKey, ignored -> new AtomicLong(0)).incrementAndGet();
        if (cause == null) {
            log.warn("Discovery parse failure. source={} reason={} parseFailureCount={}", sourceKey, message, count);
            return new IllegalArgumentException(message);
        }
        log.warn("Discovery parse failure. source={} reason={} parseFailureCount={}", sourceKey, message, count, cause);
        return new IllegalArgumentException(message, cause);
    }

    private record DiscoveryResponse(List<DiscoveryCandidate> candidates) {
    }

    private record DiscoveryCandidate(String name, String website, String industry, String description, String location,
                                      List<String> contacts) {
    }
}
