package dev.prospectos.infrastructure.mcp.service;

import dev.prospectos.api.mcp.ProviderHealth;

import java.util.List;
import java.util.Random;

final class ProviderRoutingHealthSimulator {

    private static final List<String> STATUSES = List.of("HEALTHY", "HEALTHY", "HEALTHY", "DEGRADED", "DOWN");
    private final Random random;

    ProviderRoutingHealthSimulator(Random random) {
        this.random = random;
    }

    ProviderHealth generate(String provider) {
        var status = STATUSES.get(random.nextInt(STATUSES.size()));
        var errorRate = switch (status) {
            case "HEALTHY" -> random.nextDouble() * 0.05;
            case "DEGRADED" -> 0.1 + (random.nextDouble() * 0.15);
            case "DOWN" -> 1.0;
            default -> 0.1;
        };
        var responseTime = switch (status) {
            case "HEALTHY" -> 500 + random.nextInt(300);
            case "DEGRADED" -> 1000 + random.nextInt(500);
            case "DOWN" -> 5000;
            default -> 800;
        };
        var recommendations = switch (status) {
            case "HEALTHY" -> List.of("Provider performing well");
            case "DEGRADED" -> List.of(
                "Consider reducing traffic to this provider",
                "Monitor response times closely",
                errorRate > 0.2 ? "High error rate detected - investigate" : "Error rate within tolerance"
            );
            case "DOWN" -> List.of(
                "Provider is unavailable - route traffic elsewhere",
                "Check provider status page",
                "Consider failover to backup provider"
            );
            default -> List.of("Unknown status");
        };
        return new ProviderHealth(provider, status, responseTime, errorRate, "DOWN".equals(status) ? "Connection timeout after 5000ms" : null, recommendations);
    }

    int estimatedSavings(dev.prospectos.api.mcp.RoutingStrategy strategy) {
        return switch (strategy) {
            case COST_OPTIMIZED -> 15 + random.nextInt(20);
            case PERFORMANCE_OPTIMIZED -> -5 + random.nextInt(10);
            case BALANCED -> 5 + random.nextInt(10);
        };
    }

    long impactedQueriesPerHour() {
        return 50 + random.nextInt(200);
    }
}
