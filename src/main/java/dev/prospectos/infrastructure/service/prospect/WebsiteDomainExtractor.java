package dev.prospectos.infrastructure.service.prospect;

import java.net.URI;

final class WebsiteDomainExtractor {

    String extract(String website) {
        if (website == null || website.isBlank()) {
            return null;
        }
        try {
            String host = URI.create(website.trim()).getHost();
            if (host == null || host.isBlank()) {
                return null;
            }
            return host.startsWith("www.") ? host.substring(4) : host;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
