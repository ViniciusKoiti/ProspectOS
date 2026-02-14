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
    private final List<String> defaultSources;

    @Autowired
    public AllowedSourcesComplianceService(AllowedSourcesProperties properties) {
        this(properties.allowedSources(), properties.defaultSources());
    }

    AllowedSourcesComplianceService(List<String> allowedSources, List<String> defaultSources) {
        this.allowedSources = normalizeAllowedSources(allowedSources);
        this.defaultSources = normalizeDefaultSources(defaultSources);
    }

    public List<String> validateSources(List<String> requestedSources) {
        List<String> effectiveSources = resolveEffectiveSources(requestedSources);
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String source : effectiveSources) {
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

    private List<String> resolveEffectiveSources(List<String> requestedSources) {
        if (requestedSources == null || requestedSources.isEmpty()) {
            return defaultSources;
        }

        boolean hasAnyNonBlank = requestedSources.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .anyMatch(source -> !source.isBlank());
        return hasAnyNonBlank ? requestedSources : defaultSources;
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

    private List<String> normalizeDefaultSources(List<String> sources) {
        if (sources == null || sources.isEmpty()) {
            return List.of();
        }

        return sources.stream()
            .filter(Objects::nonNull)
            .map(this::normalize)
            .filter(source -> !source.isBlank())
            .distinct()
            .toList();
    }

    private String normalize(String source) {
        return source.trim().toLowerCase(Locale.ROOT);
    }
}
