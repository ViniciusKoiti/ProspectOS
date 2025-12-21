package dev.prospectos.ai.dto;

/**
 * Priority enum specific to the AI module.
 * Avoids direct dependency on core.domain.Priority.
 */
public enum PriorityLevel {
    HOT("Immediate action required - high priority prospect"),
    WARM("Good prospect, follow up within a week"),
    COLD("Low priority, nurture for future opportunities"),
    IGNORE("Not a good fit for our ICP");

    private final String description;

    PriorityLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
    
    /**
     * Converts core.domain.Priority to ai.dto.PriorityLevel.
     */
    public static PriorityLevel fromCorePriority(String corePriority) {
        return switch (corePriority.toUpperCase()) {
            case "HOT" -> HOT;
            case "WARM" -> WARM;
            case "COLD" -> COLD;
            case "IGNORE" -> IGNORE;
            default -> COLD; // safe fallback
        };
    }
}
