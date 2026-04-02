package dev.prospectos.infrastructure.service.discovery;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "prospectos.leads.google-places")
public record GooglePlacesProperties(
    boolean enabled,
    String apiKey,
    String languageCode,
    Integer maxResults,
    String fieldMask
) {
    int resolveMaxResults(int requestedLimit) {
        int configured = maxResults == null ? requestedLimit : maxResults;
        return Math.min(Math.max(configured, 1), 20);
    }

    String normalizedLanguageCode() {
        return languageCode == null || languageCode.isBlank() ? "pt-BR" : languageCode.trim();
    }

    String normalizedFieldMask() {
        return fieldMask == null || fieldMask.isBlank()
            ? "places.displayName,places.formattedAddress,places.websiteUri,places.nationalPhoneNumber,places.types"
            : fieldMask.trim();
    }
}
