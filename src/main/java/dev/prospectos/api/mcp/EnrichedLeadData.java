package dev.prospectos.api.mcp;

import java.util.List;
import java.util.Objects;

/**
 * Enriched lead with additional intelligence.
 */
public record EnrichedLeadData(
    LeadData basicData,
    CompanyIntelligence intelligence,
    List<ContactData> contacts,
    TechnologyStack techStack,
    double enrichmentScore
) {

    public EnrichedLeadData {
        Objects.requireNonNull(basicData, "basicData must not be null");
        Objects.requireNonNull(intelligence, "intelligence must not be null");
        contacts = List.copyOf(Objects.requireNonNull(contacts, "contacts must not be null"));
        Objects.requireNonNull(techStack, "techStack must not be null");
    }
}
