package dev.prospectos.api.mcp;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record RoutingUpdate(
    boolean configurationApplied,
    RoutingStrategy strategy,
    List<String> providerPriority,
    String summary,
    Instant updatedAt,
    String previousStrategy,
    int estimatedSavingsPercent,
    long impactedQueriesPerHour,
    boolean rollbackPossible
) {

    public RoutingUpdate {
        Objects.requireNonNull(strategy, "strategy must not be null");
        Objects.requireNonNull(summary, "summary must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        providerPriority = providerPriority == null ? List.of() : List.copyOf(providerPriority);
    }

    public RoutingUpdate(
        boolean configurationApplied,
        RoutingStrategy strategy,
        List<String> providerPriority,
        String summary,
        Instant updatedAt
    ) {
        this(configurationApplied, strategy, providerPriority, summary, updatedAt, null, 0, 0L, true);
    }

    public RoutingUpdate(
        boolean success,
        String message,
        String previousStrategy,
        String newStrategy,
        int estimatedSavingsPercent,
        long impactedQueriesPerHour,
        boolean rollbackPossible
    ) {
        this(
            success,
            RoutingStrategy.fromValue(newStrategy),
            List.of(),
            message,
            Instant.now(),
            previousStrategy,
            estimatedSavingsPercent,
            impactedQueriesPerHour,
            rollbackPossible
        );
    }

    public boolean success() {
        return configurationApplied;
    }

    public String message() {
        return summary;
    }

    public String newStrategy() {
        return strategy.name();
    }
}
