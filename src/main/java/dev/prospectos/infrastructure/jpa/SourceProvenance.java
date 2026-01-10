package dev.prospectos.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "source_provenance")
public class SourceProvenance {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "company_external_id")
    private Long companyExternalId;

    @Column(name = "source_name")
    private String sourceName;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "captured_at")
    private Instant capturedAt;

    protected SourceProvenance() {
        // For JPA
    }

    private SourceProvenance(Long companyExternalId, String sourceName, String sourceUrl, Instant capturedAt) {
        this.id = UUID.randomUUID();
        this.companyExternalId = companyExternalId;
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
        this.capturedAt = capturedAt;
    }

    public static SourceProvenance of(Long companyExternalId, String sourceName, String sourceUrl, Instant capturedAt) {
        return new SourceProvenance(companyExternalId, sourceName, sourceUrl, capturedAt);
    }

    public UUID getId() {
        return id;
    }

    public Long getCompanyExternalId() {
        return companyExternalId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public Instant getCapturedAt() {
        return capturedAt;
    }
}
