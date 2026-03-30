package dev.prospectos.infrastructure.mcp.dto;

import dev.prospectos.api.mcp.RoutingUpdate;

public record RoutingUpdateResponse(
    boolean success,
    String message,
    String previousStrategy,
    String newStrategy,
    int estimatedSavingsPercent,
    long impactedQueriesPerHour,
    boolean rollbackPossible
) {

    public static RoutingUpdateResponse fromDomain(RoutingUpdate update) {
        return new RoutingUpdateResponse(
            update.success(),
            update.message(),
            update.previousStrategy(),
            update.newStrategy(),
            update.estimatedSavingsPercent(),
            update.impactedQueriesPerHour(),
            update.rollbackPossible()
        );
    }
}