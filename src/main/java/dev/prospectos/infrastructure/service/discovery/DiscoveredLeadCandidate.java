package dev.prospectos.infrastructure.service.discovery;

import java.util.List;

import dev.prospectos.api.dto.CompanyCandidateDTO;

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
    String sourceName,
    CompanyCandidateDTO.WebsitePresence websitePresence
) {
    public DiscoveredLeadCandidate(
        String name,
        String website,
        String industry,
        String description,
        String location,
        List<String> contacts,
        String sourceName
    ) {
        this(name, website, industry, description, location, contacts, sourceName, defaultWebsitePresence(website));
    }

    private static CompanyCandidateDTO.WebsitePresence defaultWebsitePresence(String website) {
        return website == null || website.isBlank()
            ? CompanyCandidateDTO.WebsitePresence.NO_WEBSITE
            : CompanyCandidateDTO.WebsitePresence.HAS_WEBSITE;
    }
}
