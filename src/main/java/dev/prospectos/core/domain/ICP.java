package dev.prospectos.core.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "icp")
public class ICP {
    @Id
    @Column(name = "id")
    private UUID id;
    @Column(name = "external_id", nullable = false, unique = true)
    private Long externalId;
    private String name;
    private String description;
    @ElementCollection
    @CollectionTable(name = "icp_industries")
    private List<String> industries;
    @ElementCollection
    @CollectionTable(name = "icp_regions")
    private List<String> regions;
    @ElementCollection
    @CollectionTable(name = "icp_target_roles")
    private List<String> targetRoles;
    private String interestTheme;

    protected ICP() { }

    private ICP(String name, String description, List<String> industries, List<String> regions,
                List<String> targetRoles, String interestTheme, Long externalId) {
        this.id = UUID.randomUUID();
        this.externalId = externalId != null ? validateExternalId(externalId) : ExternalIdPolicy.fromUuid(this.id);
        this.name = validateName(name);
        this.description = description;
        this.industries = mutableCopy(industries);
        this.regions = mutableCopy(regions);
        this.targetRoles = mutableCopy(targetRoles);
        this.interestTheme = interestTheme;
    }

    public static ICP create(String name, String description, List<String> industries, List<String> regions,
                             List<String> targetRoles, String interestTheme) {
        return new ICP(name, description, industries, regions, targetRoles, interestTheme, null);
    }

    public static ICP createWithExternalId(Long externalId, String name, String description, List<String> industries,
                                           List<String> regions, List<String> targetRoles, String interestTheme) {
        return new ICP(name, description, industries, regions, targetRoles, interestTheme, externalId);
    }

    public void updateProfile(String name, String description, List<String> industries, List<String> regions,
                              List<String> targetRoles, String interestTheme) {
        this.name = validateName(name);
        this.description = description;
        this.industries = mutableCopy(industries);
        this.regions = mutableCopy(regions);
        this.targetRoles = mutableCopy(targetRoles);
        this.interestTheme = interestTheme;
    }

    public void normalizeExternalId(Long externalId) { this.externalId = validateExternalId(externalId); }
    private List<String> mutableCopy(List<String> values) { return values == null ? new ArrayList<>() : new ArrayList<>(values); }

    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("ICP name cannot be null or empty");
        }
        return name.trim();
    }

    @PrePersist
    void ensureExternalId() {
        if (!ExternalIdPolicy.isSafe(this.externalId) && this.id != null) {
            this.externalId = ExternalIdPolicy.fromUuid(this.id);
        }
    }

    private Long validateExternalId(Long externalId) { return ExternalIdPolicy.requireSafe(externalId, "ICP externalId"); }
    public UUID getId() { return id; }
    public Long getExternalId() { return externalId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<String> getIndustries() { return industries; }
    public List<String> getRegions() { return regions; }
    public List<String> getTargetRoles() { return targetRoles; }
    public String getInterestTheme() { return interestTheme; }
}
