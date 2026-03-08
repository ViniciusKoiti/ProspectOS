package dev.prospectos.ai.client;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

final class LlmScrapingPayloadNormalizer {

    private static final Set<String> NULL_LIKE = Set.of(
        "none", "null", "n/a", "na", "not available", "unknown", "sem dados", "nao disponivel", "nÃ£o disponÃ­vel"
    );

    Map<String, Object> normalize(Map<String, Object> raw, String originalResponse) {
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

    Map<String, Object> fallback(String response, String parseError) {
        Map<String, Object> fallback = new LinkedHashMap<>();
        fallback.put("description", response == null ? "" : response.trim());
        fallback.put("source", "ai_web_search");
        fallback.put("ai_processed", true);
        if (parseError != null) {
            fallback.put("parse_error", parseError);
        }
        return fallback;
    }

    private String normalizeScalar(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        if (text.isBlank()) {
            return null;
        }
        return NULL_LIKE.contains(text.toLowerCase(Locale.ROOT)) ? null : text;
    }

    private List<String> normalizeStringList(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(this::normalizeScalar).filter(v -> v != null && !v.isBlank()).toList();
        }
        String scalar = normalizeScalar(value);
        return scalar == null ? List.of() : List.of(scalar);
    }
}
