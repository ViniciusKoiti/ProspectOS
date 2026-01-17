package dev.prospectos.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for lead search functionality.
 */
@Component
@ConfigurationProperties(prefix = "prospectos.leads")
public record LeadSearchProperties(
    Long defaultIcpId
) {
}
