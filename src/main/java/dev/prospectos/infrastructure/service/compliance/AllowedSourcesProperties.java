package dev.prospectos.infrastructure.service.compliance;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "prospectos.leads")
public record AllowedSourcesProperties(
    List<String> allowedSources,
    List<String> defaultSources
) {
}
