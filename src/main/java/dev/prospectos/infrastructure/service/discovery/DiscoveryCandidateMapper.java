package dev.prospectos.infrastructure.service.discovery;

import java.util.List;

final class DiscoveryCandidateMapper {

    DiscoveredLeadCandidate toCandidate(
        String sourceName,
        String name,
        String website,
        String industry,
        String description,
        String location,
        List<String> contacts
    ) {
        String normalizedName = asText(name);
        String normalizedWebsite = normalizeWebsite(asText(website));
        if (normalizedName == null || normalizedWebsite == null) {
            return null;
        }
        return new DiscoveredLeadCandidate(
            normalizedName,
            normalizedWebsite,
            defaultIfBlank(asText(industry), "Other"),
            defaultIfBlank(asText(description), ""),
            asText(location),
            asStringList(contacts),
            sourceName
        );
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
}
