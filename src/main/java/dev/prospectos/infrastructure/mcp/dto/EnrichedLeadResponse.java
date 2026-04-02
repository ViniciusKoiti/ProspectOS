package dev.prospectos.infrastructure.mcp.dto;

import dev.prospectos.api.mcp.*;
import java.util.List;
import java.util.Map;

public record EnrichedLeadResponse(
    LeadResponse basicData,
    CompanyIntelligenceResponse intelligence,
    List<ContactResponse> contacts,
    TechnologyStackResponse techStack,
    double enrichmentScore
) {

    public static EnrichedLeadResponse fromDomain(EnrichedLeadData enriched) {
        return new EnrichedLeadResponse(
            LeadResponse.fromDomain(enriched.basicData()),
            CompanyIntelligenceResponse.fromDomain(enriched.intelligence()),
            enriched.contacts().stream().map(ContactResponse::fromDomain).toList(),
            TechnologyStackResponse.fromDomain(enriched.techStack()),
            enriched.enrichmentScore()
        );
    }

    public record LeadResponse(
        String id, String companyName, String website, String industry, 
        String country, String city, Map<String, Object> additionalData, double qualityScore
    ) {
        public static LeadResponse fromDomain(LeadData lead) {
            return new LeadResponse(lead.id(), lead.companyName(), lead.website(),
                lead.industry(), lead.country(), lead.city(), lead.additionalData(), lead.qualityScore());
        }
    }

    public record CompanyIntelligenceResponse(
        int estimatedEmployees, String revenue, String fundingStage,
        List<String> keyPersons, Map<String, Object> socialMetrics
    ) {
        public static CompanyIntelligenceResponse fromDomain(CompanyIntelligence intelligence) {
            return new CompanyIntelligenceResponse(intelligence.estimatedEmployees(),
                intelligence.revenue(), intelligence.fundingStage(), intelligence.keyPersons(), intelligence.socialMetrics());
        }
    }

    public record ContactResponse(String name, String role, String email, String linkedIn, double confidence) {
        public static ContactResponse fromDomain(ContactData contact) {
            return new ContactResponse(contact.name(), contact.role(), contact.email(), contact.linkedIn(), contact.confidence());
        }
    }

    public record TechnologyStackResponse(List<String> frameworks, List<String> platforms, List<String> tools, String hostingProvider) {
        public static TechnologyStackResponse fromDomain(TechnologyStack techStack) {
            return new TechnologyStackResponse(techStack.frameworks(), techStack.platforms(), techStack.tools(), techStack.hostingProvider());
        }
    }
}
