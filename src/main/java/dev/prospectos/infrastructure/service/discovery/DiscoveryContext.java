package dev.prospectos.infrastructure.service.discovery;

import dev.prospectos.api.dto.ICPDto;

/**
 * Context passed to discovery source strategies.
 */
public record DiscoveryContext(
    String query,
    String role,
    int limit,
    ICPDto icp
) {
}
