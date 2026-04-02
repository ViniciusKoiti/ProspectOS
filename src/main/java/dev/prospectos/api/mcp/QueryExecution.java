package dev.prospectos.api.mcp;

import java.util.Objects;

/**
 * Individual query execution record.
 */
public record QueryExecution(
    String timestamp,
    String query,
    String provider,
    boolean success,
    long responseTimeMs,
    double cost,
    String errorMessage
) {

    public QueryExecution {
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        Objects.requireNonNull(query, "query must not be null");
        Objects.requireNonNull(provider, "provider must not be null");
    }
}
