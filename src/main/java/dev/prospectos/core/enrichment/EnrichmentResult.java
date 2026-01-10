package dev.prospectos.core.enrichment;

import dev.prospectos.core.domain.Company.CompanySize;
import dev.prospectos.core.domain.Website;

import java.util.List;

/**
 * Clean, validated data ready for domain entities.
 * This represents the processed output that can be safely used to create Company and Contact objects.
 */
public record EnrichmentResult(
    String normalizedCompanyName,
    String cleanDescription,
    List<ValidatedContact> validatedContacts,
    String normalizedPhone,
    List<String> technologies,
    String standardizedIndustry,
    CompanySize size,
    List<String> recentNews,
    Website website,
    EnrichmentQuality quality
) {

    public EnrichmentResult {
        // Null-safe initialization
        if (validatedContacts == null) {
            validatedContacts = List.of();
        }
        if (technologies == null) {
            technologies = List.of();
        }
        if (recentNews == null) {
            recentNews = List.of();
        }
    }

    public boolean hasValidContacts() {
        return validatedContacts.stream().anyMatch(ValidatedContact::isUsable);
    }

    public List<ValidatedContact> getPriorityContacts() {
        return validatedContacts.stream()
            .filter(ValidatedContact::isPriority)
            .toList();
    }

    public List<ValidatedContact> getUsableContacts() {
        return validatedContacts.stream()
            .filter(ValidatedContact::isUsable)
            .toList();
    }

    public boolean isEnrichmentSuccessful() {
        return normalizedCompanyName != null &&
               !normalizedCompanyName.trim().isEmpty() &&
               website != null &&
               hasValidContacts();
    }
}