package dev.prospectos.infrastructure.service.discovery;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "prospectos.leads.apify")
public record ApifyProperties(
    boolean enabled,
    String apiToken,
    String actorId,
    String baseUrl,
    Duration timeout,
    boolean useApifyProxy
) {
    String normalizedActorId() {
        return actorId == null || actorId.isBlank() ? "scraper-engine~google-maps-scraper" : actorId.trim();
    }

    String normalizedBaseUrl() {
        return baseUrl == null || baseUrl.isBlank() ? "https://api.apify.com" : baseUrl.trim();
    }

    Duration normalizedTimeout() {
        return timeout == null || timeout.isNegative() || timeout.isZero() ? Duration.ofSeconds(45) : timeout;
    }
}
