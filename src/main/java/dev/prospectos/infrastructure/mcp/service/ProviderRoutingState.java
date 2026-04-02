package dev.prospectos.infrastructure.mcp.service;

import java.util.List;

import dev.prospectos.api.mcp.RoutingStrategy;

final class ProviderRoutingState {

    private RoutingStrategy strategy = RoutingStrategy.BALANCED;
    private List<String> providerPriority = List.of();

    synchronized RoutingStrategy strategy() {
        return strategy;
    }

    synchronized List<String> providerPriority() {
        return providerPriority;
    }

    synchronized void update(RoutingStrategy strategy, List<String> providerPriority) {
        this.strategy = strategy;
        this.providerPriority = providerPriority == null ? List.of() : List.copyOf(providerPriority);
    }
}
