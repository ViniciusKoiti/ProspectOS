package dev.prospectos.core.domain;

/**
 * Priority levels for prospect scoring
 */
public enum Priority {
    HOT("Hot"),
    WARM("Warm"),
    COLD("Cold"),
    IGNORE("Ignore");
    
    private final String description;
    
    Priority(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}