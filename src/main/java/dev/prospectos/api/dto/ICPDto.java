package dev.prospectos.api.dto;

import java.util.List;

/**
 * Public DTO to transfer ICP data across modules.
 */
public record ICPDto(
    Long id,
    String name,
    String description,
    List<String> targetIndustries,
    List<String> regions,
    List<String> targetTechnologies,
    Integer minEmployeeCount,
    Integer maxEmployeeCount,
    List<String> targetRoles,
    String interestTheme
) {
    
    /**
     * Creates an ICPDto with minimal data for tests.
     */
    public static ICPDto createMock() {
        return new ICPDto(
            1L,
            "DevOps Teams",
            "Target companies with active DevOps practices",
            List.of("Software", "Technology", "SaaS"),
            List.of("North America", "Europe"),
            List.of("Docker", "Kubernetes", "AWS", "Jenkins"),
            50,
            500,
            List.of("CTO", "DevOps Engineer", "Platform Engineer"),
            "DevOps transformation and cloud migration"
        );
    }
}
