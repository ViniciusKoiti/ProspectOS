package dev.prospectos.core.api.dto;

/**
 * DTO público para transferir dados de Company entre módulos
 * Expõe apenas os dados necessários para outros módulos
 */
public record CompanyDTO(
    Long id,
    String name,
    String industry,
    String website,
    String description,
    Integer employeeCount,
    String location
) {
    
    /**
     * Cria um CompanyDTO com dados mínimos para testes
     */
    public static CompanyDTO createMock() {
        return new CompanyDTO(
            1L,
            "TechCorp",
            "Software",
            "https://techcorp.com",
            "Leading software company",
            150,
            "San Francisco, CA"
        );
    }
}