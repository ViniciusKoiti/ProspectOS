package dev.prospectos.infrastructure.mcp.dto;

import dev.prospectos.api.mcp.ProviderHealth;

import java.util.List;

public record ProviderHealthResponse(
    String provider,
    String status,
    long responseTimeMs,
    double errorRate,
    String lastError,
    List<String> recommendations
) {

    public static ProviderHealthResponse fromDomain(ProviderHealth health) {
        return new ProviderHealthResponse(
            health.provider(),
            health.status(),
            health.responseTime(),
            health.errorRate(),
            health.lastError(),
            health.recommendations()
        );
    }
}