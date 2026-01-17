package dev.prospectos.api.dto;

import java.util.List;

/**
 * DTO representing a company candidate from lead search (preview).
 * Unlike CompanyDTO, this does not have an id since the company hasn't been persisted yet.
 */
public record CompanyCandidateDTO(
    String name,
    String website,
    String industry,
    String description,
    String size,
    String location,
    List<String> contacts
) {
    
    /**
     * Creates a candidate with minimal data for tests.
     */
    public static CompanyCandidateDTO createMock() {
        return new CompanyCandidateDTO(
            "TechCorp",
            "https://techcorp.com",
            "Software",
            "Leading software company",
            "MEDIUM",
            "San Francisco, CA",
            List.of("contact@techcorp.com", "info@techcorp.com")
        );
    }
}
