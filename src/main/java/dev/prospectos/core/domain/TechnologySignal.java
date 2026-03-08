package dev.prospectos.core.domain;

import java.time.Instant;

import jakarta.persistence.Embeddable;

@Embeddable
public class TechnologySignal {

    private final String technology;
    private final String description;
    private final Instant detectedAt;

    protected TechnologySignal() {
        this.technology = null;
        this.description = null;
        this.detectedAt = null;
    }

    public TechnologySignal(String technology, String description, Instant detectedAt) {
        this.technology = technology;
        this.description = description;
        this.detectedAt = detectedAt;
    }

    public String getTechnology() {
        return technology;
    }

    public String getDescription() {
        return description;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }
}
