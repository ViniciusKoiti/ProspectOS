package dev.prospectos.core.domain.events;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Domain event fired when a prospecting signal is detected for a company.
 */
public record SignalDetected(
    UUID companyId,
    String companyName,
    SignalType signalType,
    String signalDescription,
    Map<String, Object> signalData,
    Instant occurredOn
) {
    public SignalDetected(UUID companyId, String companyName, SignalType signalType, 
                         String signalDescription, Map<String, Object> signalData) {
        this(companyId, companyName, signalType, signalDescription, signalData, Instant.now());
    }
    
    public enum SignalType {
        TECHNOLOGY_ADOPTION("Technology Adoption"),
        HIRING_ACTIVITY("Hiring Activity"),
        FUNDING_EVENT("Funding Event"),
        MARKET_EXPANSION("Market Expansion"),
        LEADERSHIP_CHANGE("Leadership Change"),
        PARTNERSHIP_ANNOUNCEMENT("Partnership Announcement"),
        PRODUCT_LAUNCH("Product Launch"),
        SOCIAL_MEDIA_ACTIVITY("Social Media Activity");
        
        private final String description;
        
        SignalType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}