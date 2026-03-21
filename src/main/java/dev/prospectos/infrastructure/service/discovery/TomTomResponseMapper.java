package dev.prospectos.infrastructure.service.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class TomTomResponseMapper {

    private final String sourceName;

    TomTomResponseMapper(String sourceName) {
        this.sourceName = sourceName;
    }

    List<DiscoveredLeadCandidate> toCandidates(Map<String, Object> payload, int limit) {
        if (payload == null || limit <= 0) {
            return List.of();
        }
        Object rawResults = payload.get("results");
        if (!(rawResults instanceof List<?> results)) {
            return List.of();
        }
        List<DiscoveredLeadCandidate> mapped = new ArrayList<>();
        for (Object item : results) {
            if (!(item instanceof Map<?, ?> row)) {
                continue;
            }
            DiscoveredLeadCandidate candidate = toCandidate(row);
            if (candidate != null) {
                mapped.add(candidate);
            }
            if (mapped.size() >= limit) {
                break;
            }
        }
        return List.copyOf(mapped);
    }

    private DiscoveredLeadCandidate toCandidate(Map<?, ?> row) {
        Map<?, ?> poi = row.get("poi") instanceof Map<?, ?> value ? value : Map.of();
        Map<?, ?> address = row.get("address") instanceof Map<?, ?> value ? value : Map.of();
        String name = text(poi.get("name"));
        if (name == null) {
            return null;
        }
        String website = text(poi.get("url"));
        String location = text(address.get("freeformAddress"));
        String phone = text(poi.get("phone"));
        String industry = firstCategory(poi.get("categories"));
        List<String> contacts = phone == null ? List.of() : List.of(phone);
        return new DiscoveredLeadCandidate(
            name,
            website,
            industry == null ? "other" : industry,
            "TomTom place candidate",
            location,
            contacts,
            sourceName
        );
    }

    private String firstCategory(Object value) {
        if (!(value instanceof List<?> categories) || categories.isEmpty()) {
            return null;
        }
        String first = text(categories.getFirst());
        return first == null ? null : first.toLowerCase(Locale.ROOT);
    }

    private String text(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isBlank() ? null : text;
    }
}
