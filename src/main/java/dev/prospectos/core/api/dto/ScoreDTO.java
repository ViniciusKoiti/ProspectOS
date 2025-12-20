package dev.prospectos.core.api.dto;

/**
 * DTO público para transferir dados de Score entre módulos
 */
public record ScoreDTO(
    int value,
    String category,
    String reasoning
) {
    
    /**
     * Cria um ScoreDTO com dados mínimos para testes
     */
    public static ScoreDTO createMock() {
        return new ScoreDTO(
            85,
            "HOT",
            "High fit company with strong technology stack"
        );
    }
}