package dev.prospectos.api.dto;

/**
 * Public DTO to transfer Company data across modules.
 * Exposes only the data needed by other modules.
 */
public record CompanyDTO(
    Long id,
    String name,
    String industry,
    String website,
    String description,
    Integer employeeCount,
    String location,
    ScoreDTO score,
    String primaryContactEmail,
    Integer contactCount
) {

    public CompanyDTO(
        Long id,
        String name,
        String industry,
        String website,
        String description,
        Integer employeeCount,
        String location,
        ScoreDTO score
    ) {
        this(id, name, industry, website, description, employeeCount, location, score, null, 0);
    }
    
    /**
     * Creates a CompanyDTO with minimal data for tests.
     */
    public static CompanyDTO createMock() {
        return new CompanyDTO(
            1L,
            "TechCorp",
            "Software",
            "https://techcorp.com",
            "Leading software company",
            150,
            "San Francisco, CA",
            new ScoreDTO(75, "WARM", "Good fit"),
            "contact@techcorp.com",
            1
        );
    }
}
