package dev.prospectos.infrastructure.service.inmemory;

import java.util.List;
import java.util.Objects;

final class InMemoryLeadSourceResolver {

    private static final String NO_SOURCE_CONFIGURED_MESSAGE =
        "No lead sources configured. Configure prospectos.leads.default-sources or provide sources in request";

    private InMemoryLeadSourceResolver() {
    }

    static String resolve(List<String> sources) {
        if (sources == null || sources.isEmpty()) {
            throw new IllegalArgumentException(NO_SOURCE_CONFIGURED_MESSAGE);
        }
        return sources.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(source -> !source.isBlank())
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(NO_SOURCE_CONFIGURED_MESSAGE));
    }
}
