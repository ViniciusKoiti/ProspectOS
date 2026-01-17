package dev.prospectos.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for lead search functionality.
 */
@ConfigurationProperties(prefix = "prospectos.leads")
public record LeadSearchProperties(
    Long defaultIcpId
) {
}
