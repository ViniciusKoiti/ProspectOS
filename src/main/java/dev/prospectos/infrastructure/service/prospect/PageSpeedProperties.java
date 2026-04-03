package dev.prospectos.infrastructure.service.prospect;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "prospectos.prospect.pagespeed")
public record PageSpeedProperties(
    boolean enabled,
    String apiKey,
    String baseUrl,
    String strategy,
    String locale,
    Duration timeout
) {
    String normalizedBaseUrl() {
        return baseUrl == null || baseUrl.isBlank()
            ? "https://www.googleapis.com/pagespeedonline/v5/runPagespeed"
            : baseUrl.trim();
    }

    String normalizedStrategy() {
        return "desktop".equalsIgnoreCase(strategy) ? "desktop" : "mobile";
    }

    String normalizedLocale() {
        return locale == null || locale.isBlank() ? "pt-BR" : locale.trim();
    }

    Duration normalizedTimeout() {
        return timeout == null || timeout.isNegative() || timeout.isZero() ? Duration.ofSeconds(30) : timeout;
    }
}
