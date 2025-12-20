package dev.prospectos.core.domain;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;

/**
 * Ideal Customer Profile - defines target prospect criteria
 */
@Entity
@Table(name = "icp")
public class ICP {
    
    @Id
    @Column(name = "id")
    private UUID id;
    
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
    
    protected ICP() {
        // For JPA
    }
    
    private ICP(String name, String description, List<String> industries, 
               List<String> regions, List<String> targetRoles, String interestTheme) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.industries = industries;
        this.regions = regions;
        this.targetRoles = targetRoles;
        this.interestTheme = interestTheme;
    }
    
    public static ICP create(String name, String description, List<String> industries,
                           List<String> regions, List<String> targetRoles, String interestTheme) {
        return new ICP(name, description, industries, regions, targetRoles, interestTheme);
    }
    
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<String> getIndustries() { return industries; }
    public List<String> getRegions() { return regions; }
    public List<String> getTargetRoles() { return targetRoles; }
    public String getInterestTheme() { return interestTheme; }
}