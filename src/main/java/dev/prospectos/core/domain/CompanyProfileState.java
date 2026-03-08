package dev.prospectos.core.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.domain.AbstractAggregateRoot;

@MappedSuperclass
abstract class CompanyProfileState extends AbstractAggregateRoot<Company> {

    private String description;
    private CompanySize size;
    private String country;
    private String city;
    private Instant lastUpdatedAt;

    @Column(columnDefinition = "TEXT")
    private String aiAnalysis;

    @Column(columnDefinition = "TEXT")
    private String recommendedApproach;

    protected CompanyProfileState() {
    }

    public String getDescription() {
        return description;
    }

    public CompanySize getSize() {
        return size;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public Instant getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public String getAiAnalysis() {
        return aiAnalysis;
    }

    public String getRecommendedApproach() {
        return recommendedApproach;
    }

    public void setDescription(String description) {
        this.description = description;
        touch();
    }

    public void setSize(CompanySize size) {
        this.size = size;
        touch();
    }

    public void setLocation(String country, String city) {
        this.country = country;
        this.city = city;
        touch();
    }

    public void setAiAnalysis(String aiAnalysis) {
        this.aiAnalysis = aiAnalysis;
        touch();
    }

    public void setRecommendedApproach(String recommendedApproach) {
        this.recommendedApproach = recommendedApproach;
        touch();
    }

    public String getLocation() {
        if (city != null && country != null) {
            return city + ", " + country;
        }
        if (country != null) {
            return country;
        }
        return "Unknown";
    }

    protected void initializeLastUpdatedAt(Instant value) {
        this.lastUpdatedAt = value;
    }

    protected void touch() {
        this.lastUpdatedAt = Instant.now();
    }
}
