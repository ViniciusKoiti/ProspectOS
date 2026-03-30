package dev.prospectos.infrastructure.mcp.dto;

import dev.prospectos.api.mcp.ProviderHealth;

import java.util.List;

public record ProviderHealthStatus(
    String provider,
    String status,
    long responseTime,
    double errorRate,
    String lastError,
    List<String> recommendations
) {

    public static ProviderHealthStatus fromDomain(ProviderHealth providerHealth) {
        return new ProviderHealthStatus(
            providerHealth.provider(),
            providerHealth.status(),
            providerHealth.responseTime(),
            providerHealth.errorRate(),
            providerHealth.lastError(),
            providerHealth.recommendations()
        );
    }
}
