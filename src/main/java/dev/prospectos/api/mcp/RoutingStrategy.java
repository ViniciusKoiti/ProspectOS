package dev.prospectos.api.mcp;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public enum RoutingStrategy {

    COST_OPTIMIZED,
    PERFORMANCE_OPTIMIZED,
    BALANCED;

    public static RoutingStrategy fromValue(String value) {
        var normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
            .filter(strategy -> strategy.name().equals(normalized))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Invalid routing strategy '%s'. Allowed values: %s".formatted(value, supportedValues())
            ));
    }

    public static String supportedValues() {
        return Arrays.stream(values())
            .map(RoutingStrategy::name)
            .collect(Collectors.joining(", "));
    }
}
