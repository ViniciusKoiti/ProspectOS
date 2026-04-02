package dev.prospectos.infrastructure.mcp.tools;

import dev.prospectos.infrastructure.service.compliance.AllowedSourcesProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
class ProviderRoutingMcpInputParser {

    private final AllowedSourcesProperties allowedSourcesProperties;

    ProviderRoutingMcpInputParser(AllowedSourcesProperties allowedSourcesProperties) {
        this.allowedSourcesProperties = allowedSourcesProperties;
    }

    List<String> parseProviderPriority(String providerPriority) {
        if (!StringUtils.hasText(providerPriority)) {
            return List.of();
        }
        var providers = Arrays.stream(providerPriority.split(","))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .map(value -> value.toLowerCase(Locale.ROOT))
            .toList();
        var invalidProviders = providers.stream().filter(provider -> !supportedProviders().contains(provider)).toList();
        if (!invalidProviders.isEmpty()) {
            throw new IllegalArgumentException(
                "Invalid providers: %s. Allowed values: %s".formatted(
                    String.join(", ", invalidProviders),
                    String.join(", ", supportedProviders())
                )
            );
        }
        return providers;
    }

    Map<String, String> parseConditions(String conditions) {
        if (!StringUtils.hasText(conditions)) {
            return Map.of();
        }
        return Arrays.stream(conditions.split(","))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .filter(condition -> condition.contains("="))
            .collect(Collectors.toMap(
                condition -> condition.substring(0, condition.indexOf("=")).trim(),
                condition -> condition.substring(condition.indexOf("=") + 1).trim(),
                (existing, replacement) -> replacement,
                LinkedHashMap::new
            ));
    }

    private Set<String> supportedProviders() {
        var providers = new LinkedHashSet<String>(List.of("nominatim", "bing-maps", "google-places", "scraper", "llm-discovery"));
        if (allowedSourcesProperties.allowedSources() != null) {
            allowedSourcesProperties.allowedSources().stream()
                .filter(StringUtils::hasText)
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .forEach(providers::add);
        }
        return Collections.unmodifiableSet(providers);
    }
}
