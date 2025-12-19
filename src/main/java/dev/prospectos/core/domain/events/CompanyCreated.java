package dev.prospectos.core.domain.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event fired when a new company is created.
 */
public record CompanyCreated(
    UUID companyId,
    String companyName,
    String website,
    Instant occurredOn
) {
    public CompanyCreated(UUID companyId, String companyName, String website) {
        this(companyId, companyName, website, Instant.now());
    }
}