package dev.prospectos.api.mcp;

import java.util.List;
import java.util.Objects;

public record ProviderHealth(
    String provider,
    String status,
    long responseTime,
    double errorRate,
    String lastError,
    List<String> recommendations
) {

    public ProviderHealth {
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(status, "status must not be null");
        recommendations = List.copyOf(recommendations);
    }
}
