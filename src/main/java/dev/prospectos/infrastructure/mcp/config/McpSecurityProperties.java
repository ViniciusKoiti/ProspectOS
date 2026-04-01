package dev.prospectos.infrastructure.mcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Set;

/**
 * Configuration properties for MCP security and rate limiting.
 */
@ConfigurationProperties(prefix = "spring.ai.mcp.server.security")
@ConditionalOnMcpEnabled
public record McpSecurityProperties(
    boolean enabled,
    String apiKeyHeader,
    Set<String> allowedApiKeys,
    RateLimit rateLimit,
    Audit audit
) {
    
    public McpSecurityProperties {
        // Set defaults if not provided
        if (apiKeyHeader == null || apiKeyHeader.isBlank()) {
            apiKeyHeader = "X-MCP-API-KEY";
        }
        if (allowedApiKeys == null) {
            allowedApiKeys = Set.of();
        }
        if (rateLimit == null) {
            rateLimit = new RateLimit(100, 20, Duration.ofMinutes(1), Duration.ofHours(1));
        }
        if (audit == null) {
            audit = new Audit(true, true, false);
        }
    }

    public record RateLimit(
        int requestsPerMinute,
        int burstCapacity,
        Duration windowSize,
        Duration blockDuration
    ) {
        public RateLimit {
            if (requestsPerMinute <= 0) requestsPerMinute = 100;
            if (burstCapacity <= 0) burstCapacity = 20;
            if (windowSize == null) windowSize = Duration.ofMinutes(1);
            if (blockDuration == null) blockDuration = Duration.ofHours(1);
        }
    }

    public record Audit(
        boolean enabled,
        boolean logSuccessfulRequests,
        boolean logFailedRequests
    ) {}
}


