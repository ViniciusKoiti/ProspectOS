package dev.prospectos.api.dto;

/**
 * Public DTO to transfer Score data across modules.
 */
public record ScoreDTO(
    int value,
    String category,
    String reasoning
) {
    
    /**
     * Creates a ScoreDTO with minimal data for tests.
     */
    public static ScoreDTO createMock() {
        return new ScoreDTO(
            85,
            "HOT",
            "High fit company with strong technology stack"
        );
    }
}
