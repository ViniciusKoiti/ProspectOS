package dev.prospectos.core.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

@MappedSuperclass
abstract class CompanyCoreState extends CompanyRelationshipState {

    @Id
    @Column(name = "id")
    protected UUID id;

    @Column(name = "external_id", nullable = false, unique = true)
    protected Long externalId;

    protected String name;

    @Embedded
    protected Website website;

    protected String industry;

    @Embedded
    protected Score prospectingScore;

    @Enumerated(EnumType.STRING)
    protected ProspectingStatus status;

    protected Instant createdAt;

    protected CompanyCoreState() {
    }

    protected void initializeCoreState(String name, Website website, String industry) {
        Instant now = Instant.now();
        this.id = UUID.randomUUID();
        this.externalId = ExternalIdPolicy.fromUuid(this.id);
        this.name = validateName(name);
        this.website = website;
        this.industry = industry;
        this.prospectingScore = Score.zero();
        this.status = ProspectingStatus.NEW;
        this.createdAt = now;
        initializeLastUpdatedAt(now);
    }

    @PrePersist
    void ensureExternalId() {
        if (!ExternalIdPolicy.isSafe(externalId) && id != null) {
            externalId = ExternalIdPolicy.fromUuid(id);
        }
    }

    protected void assignExternalId(Long newExternalId) {
        this.externalId = ExternalIdPolicy.requireSafe(newExternalId, "Company externalId");
    }

    public UUID getId() { return id; }
    public Long getExternalId() { return externalId; }
    public String getName() { return name; }
    public Website getWebsite() { return website; }
    public String getIndustry() { return industry; }
    public Score getProspectingScore() { return prospectingScore; }
    public ProspectingStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    protected String validateName(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Company name cannot be null or empty");
        }
        return value.trim();
    }
}
