package dev.prospectos.infrastructure.mcp.dto;

import dev.prospectos.api.mcp.RoutingUpdate;

import java.time.Instant;
import java.util.List;

public record RoutingUpdateResult(
    boolean configurationApplied,
    String strategy,
    List<String> providerPriority,
    String summary,
    Instant updatedAt
) {

    public static RoutingUpdateResult fromDomain(RoutingUpdate update) {
        return new RoutingUpdateResult(
            update.configurationApplied(),
            update.strategy().name(),
            update.providerPriority(),
            update.summary(),
            update.updatedAt()
        );
    }
}
