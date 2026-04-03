package dev.prospectos.infrastructure.service.outreach;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "prospectos.outreach.resend")
public record ResendProperties(
    boolean enabled,
    String apiKey,
    String baseUrl,
    Duration timeout
) {
    String normalizedBaseUrl() {
        return baseUrl == null || baseUrl.isBlank() ? "https://api.resend.com/emails" : baseUrl.trim();
    }

    Duration normalizedTimeout() {
        return timeout == null || timeout.isNegative() || timeout.isZero() ? Duration.ofSeconds(15) : timeout;
    }
}
