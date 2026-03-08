package dev.prospectos.infrastructure.service.inmemory;

import java.util.List;
import java.util.Objects;

final class InMemoryLeadSourceResolver {

    private static final String DEFAULT_SOURCE_NAME = "in-memory";

    private InMemoryLeadSourceResolver() {
    }

    static String resolve(List<String> sources) {
        if (sources == null || sources.isEmpty()) {
            return DEFAULT_SOURCE_NAME;
        }
        return sources.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(source -> !source.isBlank())
            .findFirst()
            .orElse(DEFAULT_SOURCE_NAME);
    }
}
