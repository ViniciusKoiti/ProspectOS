package dev.prospectos.infrastructure.service.discovery;

import java.util.List;

import dev.prospectos.api.dto.CompanyCandidateDTO;

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
        WebsiteNormalization websiteNormalization = normalizeWebsite(asText(website));
        if (normalizedName == null) {
            return null;
        }
        return new DiscoveredLeadCandidate(
            normalizedName,
            websiteNormalization.website(),
            defaultIfBlank(asText(industry), "Other"),
            defaultIfBlank(asText(description), ""),
            asText(location),
            asStringList(contacts),
            sourceName,
            websiteNormalization.presence()
        );
    }

    private WebsiteNormalization normalizeWebsite(String website) {
        if (website == null || website.isBlank()) {
            return new WebsiteNormalization(null, CompanyCandidateDTO.WebsitePresence.NO_WEBSITE);
        }
        String trimmed = website.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return new WebsiteNormalization(trimmed, CompanyCandidateDTO.WebsitePresence.HAS_WEBSITE);
        }
        if (trimmed.contains(" ")) {
            return new WebsiteNormalization(null, CompanyCandidateDTO.WebsitePresence.UNKNOWN);
        }
        return new WebsiteNormalization("https://" + trimmed, CompanyCandidateDTO.WebsitePresence.HAS_WEBSITE);
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

    private record WebsiteNormalization(String website, CompanyCandidateDTO.WebsitePresence presence) {
    }
}
