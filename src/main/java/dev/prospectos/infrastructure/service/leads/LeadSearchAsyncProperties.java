package dev.prospectos.infrastructure.service.leads;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Async lead search execution settings.
 */
@ConfigurationProperties(prefix = "prospectos.leads.async")
public record LeadSearchAsyncProperties(
    @DefaultValue("3") int maxParallelSources,
    @DefaultValue("20") int sourceTimeoutSeconds,
    @DefaultValue("60") int jobTtlMinutes
) {
}
