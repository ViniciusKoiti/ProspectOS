package dev.prospectos.infrastructure.service.discovery;

import java.util.List;

/**
 * Strategy for discovering lead candidates from a specific source.
 */
public interface LeadDiscoverySource {

    String sourceName();

    List<DiscoveredLeadCandidate> discover(DiscoveryContext context);
}
