package dev.prospectos.infrastructure.service.discovery;

import java.util.List;

/**
 * Internal normalized candidate model from discovery sources.
 */
public record DiscoveredLeadCandidate(
    String name,
    String website,
    String industry,
    String description,
    String location,
    List<String> contacts,
    String sourceName
) {
}
