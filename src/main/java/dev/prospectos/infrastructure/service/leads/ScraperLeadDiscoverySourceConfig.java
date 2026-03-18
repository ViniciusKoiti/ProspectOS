package dev.prospectos.infrastructure.service.leads;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class ScraperLeadDiscoverySourceConfig {

    private ScraperLeadDiscoverySourceConfig() {
    }

    static Set<String> fromAllowedSources(List<String> allowedSources) {
        if (allowedSources == null || allowedSources.isEmpty()) {
            return Set.of();
        }
        Set<String> discoverySources = new LinkedHashSet<>();
        for (String source : allowedSources) {
            String normalizedSource = normalize(source);
            if (normalizedSource.isBlank() || isNonDiscoverySource(normalizedSource)) {
                continue;
            }
            discoverySources.add(normalizedSource);
        }
        return Set.copyOf(discoverySources);
    }

    private static String normalize(String source) {
        if (source == null) {
            return "";
        }
        return source.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean isNonDiscoverySource(String source) {
        return "in-memory".equals(source) || "scraper".equals(source);
    }
}
