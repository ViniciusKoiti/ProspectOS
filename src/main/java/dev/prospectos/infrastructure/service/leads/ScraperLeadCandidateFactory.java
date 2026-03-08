package dev.prospectos.infrastructure.service.leads;

import java.util.List;

import dev.prospectos.api.dto.CompanyCandidateDTO;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.Website;
import dev.prospectos.core.enrichment.EnrichmentResult;
import dev.prospectos.core.enrichment.ValidatedContact;

final class ScraperLeadCandidateFactory {

    Company buildCandidateCompany(EnrichmentResult enrichmentResult, String fallbackQuery) {
        String name = enrichmentResult.normalizedCompanyName();
        if (name == null || name.isBlank()) {
            name = fallbackQuery;
        }

        String industry = enrichmentResult.standardizedIndustry();
        String websiteUrl = resolveSourceUrl(enrichmentResult, fallbackQuery);
        if (!isValidWebsite(websiteUrl)) {
            return null;
        }

        Company company = Company.create(
            name.trim(),
            Website.of(websiteUrl),
            industry == null ? "Other" : industry.trim()
        );
        if (enrichmentResult.cleanDescription() != null) {
            company.setDescription(enrichmentResult.cleanDescription());
        }
        if (enrichmentResult.size() != null) {
            company.setSize(enrichmentResult.size());
        }
        return company;
    }

    CompanyCandidateDTO toCompanyCandidateDTO(Company company, EnrichmentResult enrichmentResult) {
        List<String> contacts = enrichmentResult.validatedContacts() != null
            ? enrichmentResult.validatedContacts().stream()
                .filter(ValidatedContact::isUsable)
                .map(contact -> contact.email().getAddress())
                .toList()
            : List.of();

        return new CompanyCandidateDTO(
            company.getName(),
            company.getWebsite().getUrl(),
            company.getIndustry(),
            company.getDescription(),
            company.getSize() != null ? company.getSize().name() : null,
            null,
            contacts
        );
    }

    String resolveSourceUrl(EnrichmentResult enrichmentResult, String fallbackQuery) {
        if (enrichmentResult.website() != null) {
            return enrichmentResult.website().getUrl();
        }
        return fallbackQuery;
    }

    private boolean isValidWebsite(String website) {
        try {
            Website.of(website);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
