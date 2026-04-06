package dev.prospectos.infrastructure.service.prospect;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public final class WebsiteDomainExtractor {

    String extract(String website) {
        if (website == null || website.isBlank()) {
            return null;
        }
        String normalized = website.trim();
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://" + normalized;
        }
        try {
            URI uri = new URI(normalized);
            String host = uri.getHost();
            if (host == null || host.isBlank() || !host.contains(".")) {
                return null;
            }
            return host.startsWith("www.") ? host.substring(4) : host;
        } catch (URISyntaxException exception) {
            return null;
        }
    }
}
