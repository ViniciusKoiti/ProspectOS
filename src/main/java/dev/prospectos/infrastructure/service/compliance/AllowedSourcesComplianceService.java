package dev.prospectos.infrastructure.service.compliance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validates that requested sources are allowed.
 */
@Service
public class AllowedSourcesComplianceService {

    private final Set<String> allowedSources;

    @Autowired
    public AllowedSourcesComplianceService(AllowedSourcesProperties properties) {
        this(properties.allowedSources());
    }

    AllowedSourcesComplianceService(List<String> allowedSources) {
        this.allowedSources = normalizeAllowedSources(allowedSources);
    }

    public List<String> validateSources(List<String> requestedSources) {
        if (requestedSources == null || requestedSources.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String source : requestedSources) {
            if (source == null) {
                continue;
            }
            String normalizedSource = normalize(source);
            if (normalizedSource.isBlank()) {
                continue;
            }
            normalized.add(normalizedSource);
        }

        if (!normalized.isEmpty() && allowedSources.isEmpty()) {
            throw new IllegalArgumentException("No allowed sources configured");
        }

        for (String source : normalized) {
            if (!allowedSources.contains(source)) {
                throw new IllegalArgumentException("Disallowed source: " + source);
            }
        }

        return List.copyOf(normalized);
    }

    private Set<String> normalizeAllowedSources(List<String> sources) {
        if (sources == null || sources.isEmpty()) {
            return Set.of();
        }

        return sources.stream()
            .filter(Objects::nonNull)
            .map(this::normalize)
            .filter(source -> !source.isBlank())
            .collect(Collectors.toUnmodifiableSet());
    }

    private String normalize(String source) {
        return source.trim().toLowerCase(Locale.ROOT);
    }
}
