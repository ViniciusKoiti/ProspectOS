package dev.prospectos.core.enrichment;

import dev.prospectos.core.domain.Website;

final class EnrichmentWebsiteResolver {

    Website parsePrimary(String websiteUrl) {
        if (websiteUrl == null || websiteUrl.trim().isEmpty()) {
            return null;
        }
        try {
            Website website = Website.of(websiteUrl.trim());
            return website.getDomain().contains(".") ? website : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    Website parseFallback(String websiteUrl) {
        if (websiteUrl == null || websiteUrl.trim().isEmpty()) {
            return null;
        }
        try {
            return Website.of(websiteUrl.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
