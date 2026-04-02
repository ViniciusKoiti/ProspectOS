package dev.prospectos.infrastructure.mcp.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

record QueryMetricsObservation(
    String provider,
    String operation,
    Instant recordedAt,
    long durationMs,
    boolean success,
    int resultCount,
    BigDecimal estimatedCost
) {

    QueryMetricsObservation {
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(operation, "operation must not be null");
        Objects.requireNonNull(recordedAt, "recordedAt must not be null");
        Objects.requireNonNull(estimatedCost, "estimatedCost must not be null");
    }
}
