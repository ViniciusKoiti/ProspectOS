package dev.prospectos.infrastructure.service.prospect;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "prospectos.prospect.hunter")
public record HunterProperties(
    boolean enabled,
    String apiKey,
    String baseUrl,
    Duration timeout,
    Integer maxResults
) {
    String normalizedBaseUrl() {
        return baseUrl == null || baseUrl.isBlank() ? "https://api.hunter.io/v2/domain-search" : baseUrl.trim();
    }

    Duration normalizedTimeout() {
        return timeout == null || timeout.isNegative() || timeout.isZero() ? Duration.ofSeconds(15) : timeout;
    }

    int normalizedMaxResults() {
        int configured = maxResults == null ? 5 : maxResults;
        return Math.min(Math.max(configured, 1), 10);
    }
}
