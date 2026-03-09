package dev.prospectos.infrastructure.service.inmemory;

import dev.prospectos.core.domain.Website;

final class InMemoryWebsiteMatcher {

    String extractDomainOrNull(String website) {
        if (website == null || website.isBlank()) {
            return null;
        }
        try {
            return Website.of(website).getDomain();
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    boolean hasSameDomain(String website, String targetDomain) {
        try {
            return Website.of(website).getDomain().equalsIgnoreCase(targetDomain);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
