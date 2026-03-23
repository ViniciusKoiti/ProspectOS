package dev.prospectos.infrastructure.service.discovery;

import java.util.List;
import java.util.Locale;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "prospectos.leads.amazon-location")
public record AmazonLocationProperties(
    boolean enabled,
    String region,
    String apiKey,
    String language,
    String intendedUse,
    Integer maxResults,
    List<String> includeCountries
) {
    private static final int API_MAX_RESULTS = 100;

    String normalizedRegion() {
        if (region == null || region.isBlank()) {
            return "us-east-1";
        }
        return region.trim();
    }

    String normalizedLanguage() {
        if (language == null || language.isBlank()) {
            return "pt-BR";
        }
        return language.trim();
    }

    String normalizedIntendedUse() {
        if (intendedUse == null || intendedUse.isBlank()) {
            return "SingleUse";
        }
        return intendedUse.trim();
    }

    int resolveMaxResults(int requestedLimit) {
        int baseLimit = requestedLimit <= 0 ? 1 : requestedLimit;
        int configuredLimit = maxResults == null ? baseLimit : maxResults;
        return Math.min(Math.max(1, Math.min(baseLimit, configuredLimit)), API_MAX_RESULTS);
    }

    List<String> normalizedIncludeCountries() {
        if (includeCountries == null || includeCountries.isEmpty()) {
            return List.of();
        }
        return includeCountries.stream()
            .map(value -> value == null ? null : value.trim().toUpperCase(Locale.ROOT))
            .filter(value -> value != null && (value.length() == 2 || value.length() == 3))
            .distinct()
            .toList();
    }
}
