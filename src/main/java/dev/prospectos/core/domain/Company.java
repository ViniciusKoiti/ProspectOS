package dev.prospectos.core.domain;

import dev.prospectos.core.domain.events.CompanyCreated;
import dev.prospectos.core.domain.events.CompanyScored;
import dev.prospectos.core.domain.events.SignalDetected;
import jakarta.persistence.*;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Company aggregate root representing a B2B prospecting target.
 * Manages business logic for scoring, signal detection, and prospect qualification.
 */
@Entity
@Table(name = "companies")
public class Company extends AbstractAggregateRoot<Company> {
    
    @Id
    @Column(name = "id")
    private UUID id;
    private String name;
    @Embedded
    private Website website;
    private String industry;
    private String description;
    private CompanySize size;
    private String country;
    private String city;
    @Embedded
    private Score prospectingScore;
    @Enumerated(EnumType.STRING)
    private ProspectingStatus status;
    
    @ElementCollection
    @CollectionTable(name = "company_contacts")
    private List<Contact> contacts;
    
    @ElementCollection 
    @CollectionTable(name = "company_technology_signals")
    private List<TechnologySignal> technologySignals;
    private Instant createdAt;
    private Instant lastUpdatedAt;
    
    @Column(columnDefinition = "TEXT")
    private String aiAnalysis;
    
    @Column(columnDefinition = "TEXT")
    private String recommendedApproach;
    
    protected Company() {
        // For JPA
    }
    
    private Company(String name, Website website, String industry) {
        this.id = UUID.randomUUID();
        this.name = validateName(name);
        this.website = website;
        this.industry = industry;
        this.prospectingScore = Score.zero();
        this.status = ProspectingStatus.NEW;
        this.contacts = new ArrayList<>();
        this.technologySignals = new ArrayList<>();
        this.createdAt = Instant.now();
        this.lastUpdatedAt = Instant.now();
        
        registerEvent(new CompanyCreated(id, name, website.getUrl()));
    }
    
    public static Company create(String name, Website website, String industry) {
        return new Company(name, website, industry);
    }

    public void updateProfile(String name, Website website, String industry) {
        if (website == null) {
            throw new IllegalArgumentException("Website cannot be null");
        }
        this.name = validateName(name);
        this.website = website;
        this.industry = industry;
        this.lastUpdatedAt = Instant.now();
    }
    
    public void updateScore(Score newScore, String reason) {
        if (newScore == null) {
            throw new IllegalArgumentException("Score cannot be null");
        }
        
        Score previousScore = this.prospectingScore;
        this.prospectingScore = newScore;
        this.lastUpdatedAt = Instant.now();
        
        updateStatusBasedOnScore();
        
        registerEvent(new CompanyScored(id, name, 
            previousScore.getValue(), newScore.getValue(), reason));
    }
    
    public void addContact(Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("Contact cannot be null");
        }
        
        validateContactUniqueness(contact);
        this.contacts.add(contact);
        this.lastUpdatedAt = Instant.now();
    }
    
    public void detectSignal(SignalDetected.SignalType signalType, String description, Map<String, Object> data) {
        this.lastUpdatedAt = Instant.now();
        
        if (signalType == SignalDetected.SignalType.TECHNOLOGY_ADOPTION && data != null) {
            String technology = (String) data.get("technology");
            if (technology != null) {
                addTechnologySignal(technology, description);
            }
        }
        
        registerEvent(new SignalDetected(id, name, signalType, description, data));
    }
    
    public boolean isQualifiedProspect() {
        return prospectingScore.isHighPriority() && 
               status == ProspectingStatus.QUALIFIED &&
               hasValidContactInfo();
    }
    
    public boolean hasValidContactInfo() {
        return !contacts.isEmpty() && 
               contacts.stream().anyMatch(Contact::hasValidEmail);
    }
    
    public void qualify() {
        if (!canBeQualified()) {
            throw new IllegalStateException("Company cannot be qualified in current state");
        }
        
        this.status = ProspectingStatus.QUALIFIED;
        this.lastUpdatedAt = Instant.now();
    }
    
    public void disqualify(String reason) {
        this.status = ProspectingStatus.DISQUALIFIED;
        this.lastUpdatedAt = Instant.now();
    }
    
    private boolean canBeQualified() {
        return status == ProspectingStatus.NEW || status == ProspectingStatus.REVIEWING;
    }
    
    private void updateStatusBasedOnScore() {
        if (prospectingScore.isHighPriority() && status == ProspectingStatus.NEW) {
            this.status = ProspectingStatus.REVIEWING;
        }
    }
    
    private void validateContactUniqueness(Contact newContact) {
        boolean emailExists = contacts.stream()
            .anyMatch(contact -> contact.getEmail().equals(newContact.getEmail()));
        
        if (emailExists) {
            throw new IllegalArgumentException("Contact with email already exists: " + newContact.getEmail());
        }
    }
    
    private void addTechnologySignal(String technology, String description) {
        TechnologySignal signal = new TechnologySignal(technology, description, Instant.now());
        this.technologySignals.add(signal);
    }
    
    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Company name cannot be null or empty");
        }
        return name.trim();
    }
    
    // Getters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public Website getWebsite() { return website; }
    public String getIndustry() { return industry; }
    public String getDescription() { return description; }
    public CompanySize getSize() { return size; }
    public String getCountry() { return country; }
    public String getCity() { return city; }
    public Score getProspectingScore() { return prospectingScore; }
    public ProspectingStatus getStatus() { return status; }
    public List<Contact> getContacts() { return new ArrayList<>(contacts); }
    public List<TechnologySignal> getTechnologySignals() { return new ArrayList<>(technologySignals); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastUpdatedAt() { return lastUpdatedAt; }
    public String getAiAnalysis() { return aiAnalysis; }
    public String getRecommendedApproach() { return recommendedApproach; }
    
    // Setters for basic properties
    public void setDescription(String description) {
        this.description = description;
        this.lastUpdatedAt = Instant.now();
    }
    
    public void setSize(CompanySize size) {
        this.size = size;
        this.lastUpdatedAt = Instant.now();
    }
    
    public void setLocation(String country, String city) {
        this.country = country;
        this.city = city;
        this.lastUpdatedAt = Instant.now();
    }
    
    public void setAiAnalysis(String aiAnalysis) {
        this.aiAnalysis = aiAnalysis;
        this.lastUpdatedAt = Instant.now();
    }
    
    public void setRecommendedApproach(String recommendedApproach) {
        this.recommendedApproach = recommendedApproach;
        this.lastUpdatedAt = Instant.now();
    }
    
    public String getLocation() {
        if (city != null && country != null) {
            return city + ", " + country;
        } else if (country != null) {
            return country;
        }
        return "Unknown";
    }
    
    public boolean hasActiveSignals() {
        return !technologySignals.isEmpty();
    }
    
    public enum CompanySize {
        STARTUP("1-10 employees"),
        SMALL("11-50 employees"),
        MEDIUM("51-200 employees"),
        LARGE("201-1000 employees"),
        ENTERPRISE("1000+ employees");
        
        private final String description;
        
        CompanySize(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum ProspectingStatus {
        NEW,
        REVIEWING, 
        QUALIFIED,
        DISQUALIFIED,
        CONTACTED,
        OPPORTUNITY,
        CLOSED_WON,
        CLOSED_LOST
    }
    
    @Embeddable
    public static class Contact {
        private final String name;
        @Embedded
        private final Email email;
        private final String position;
        private final String phoneNumber;
        
        protected Contact() {
            // For JPA
            this.name = null;
            this.email = null;
            this.position = null;
            this.phoneNumber = null;
        }
        
        public Contact(String name, Email email, String position, String phoneNumber) {
            this.name = validateContactName(name);
            this.email = email;
            this.position = position;
            this.phoneNumber = phoneNumber;
        }
        
        private String validateContactName(String name) {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Contact name cannot be null or empty");
            }
            return name.trim();
        }
        
        public boolean hasValidEmail() {
            return email != null;
        }
        
        public String getName() { return name; }
        public Email getEmail() { return email; }
        public String getPosition() { return position; }
        public String getPhoneNumber() { return phoneNumber; }
    }
    
    @Embeddable
    public static class TechnologySignal {
        private final String technology;
        private final String description;
        private final Instant detectedAt;
        
        protected TechnologySignal() {
            // For JPA
            this.technology = null;
            this.description = null;
            this.detectedAt = null;
        }
        
        public TechnologySignal(String technology, String description, Instant detectedAt) {
            this.technology = technology;
            this.description = description;
            this.detectedAt = detectedAt;
        }
        
        public String getTechnology() { return technology; }
        public String getDescription() { return description; }
        public Instant getDetectedAt() { return detectedAt; }
    }
}
