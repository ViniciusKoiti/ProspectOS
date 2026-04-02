package dev.prospectos.api.mcp;

import java.util.List;
import java.util.Objects;

/**
 * Technology stack information.
 */
public record TechnologyStack(
    List<String> frameworks,
    List<String> platforms,
    List<String> tools,
    String hostingProvider
) {

    public TechnologyStack {
        frameworks = List.copyOf(Objects.requireNonNull(frameworks, "frameworks must not be null"));
        platforms = List.copyOf(Objects.requireNonNull(platforms, "platforms must not be null"));
        tools = List.copyOf(Objects.requireNonNull(tools, "tools must not be null"));
        Objects.requireNonNull(hostingProvider, "hostingProvider must not be null");
    }
}
