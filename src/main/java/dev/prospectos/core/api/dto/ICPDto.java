package dev.prospectos.core.api.dto;

import java.util.List;

/**
 * DTO público para transferir dados de ICP entre módulos
 */
public record ICPDto(
    Long id,
    String name,
    String description,
    List<String> targetIndustries,
    List<String> targetTechnologies,
    Integer minEmployeeCount,
    Integer maxEmployeeCount,
    List<String> targetRoles
) {
    
    /**
     * Cria um ICPDto com dados mínimos para testes
     */
    public static ICPDto createMock() {
        return new ICPDto(
            1L,
            "DevOps Teams",
            "Target companies with active DevOps practices",
            List.of("Software", "Technology", "SaaS"),
            List.of("Docker", "Kubernetes", "AWS", "Jenkins"),
            50,
            500,
            List.of("CTO", "DevOps Engineer", "Platform Engineer")
        );
    }
}