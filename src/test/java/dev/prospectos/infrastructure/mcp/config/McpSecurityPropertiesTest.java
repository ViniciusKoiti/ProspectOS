package dev.prospectos.infrastructure.mcp.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Set;

import org.junit.jupiter.api.Test;

class McpSecurityPropertiesTest {

    @Test
    void shouldApplyDefaultsWhenOptionalValuesAreMissing() {
        var properties = new McpSecurityProperties(true, null, null, null, null);

        assertThat(properties.apiKeyHeader()).isEqualTo("X-MCP-API-KEY");
        assertThat(properties.allowedApiKeys()).isEmpty();
        assertThat(properties.rateLimit().requestsPerMinute()).isEqualTo(100);
        assertThat(properties.rateLimit().burstCapacity()).isEqualTo(20);
        assertThat(properties.rateLimit().windowSize()).isEqualTo(Duration.ofMinutes(1));
        assertThat(properties.rateLimit().blockDuration()).isEqualTo(Duration.ofHours(1));
        assertThat(properties.audit().enabled()).isTrue();
        assertThat(properties.audit().logSuccessfulRequests()).isTrue();
        assertThat(properties.audit().logFailedRequests()).isFalse();
    }

    @Test
    void shouldKeepExplicitValues() {
        var rateLimit = new McpSecurityProperties.RateLimit(42, 7, Duration.ofSeconds(30), Duration.ofMinutes(5));
        var audit = new McpSecurityProperties.Audit(true, false, true);

        var properties = new McpSecurityProperties(false, "X-ALT", Set.of("key-1"), rateLimit, audit);

        assertThat(properties.enabled()).isFalse();
        assertThat(properties.apiKeyHeader()).isEqualTo("X-ALT");
        assertThat(properties.allowedApiKeys()).containsExactly("key-1");
        assertThat(properties.rateLimit()).isEqualTo(rateLimit);
        assertThat(properties.audit()).isEqualTo(audit);
    }

    @Test
    void shouldNormalizeInvalidRateLimitValues() {
        var rateLimit = new McpSecurityProperties.RateLimit(0, -1, null, null);

        assertThat(rateLimit.requestsPerMinute()).isEqualTo(100);
        assertThat(rateLimit.burstCapacity()).isEqualTo(20);
        assertThat(rateLimit.windowSize()).isEqualTo(Duration.ofMinutes(1));
        assertThat(rateLimit.blockDuration()).isEqualTo(Duration.ofHours(1));
    }
}
