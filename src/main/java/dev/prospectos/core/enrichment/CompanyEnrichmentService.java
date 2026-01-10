package dev.prospectos.core.enrichment;

import dev.prospectos.core.domain.Company.CompanySize;
import dev.prospectos.core.domain.Website;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Main service for enriching company data.
 * Orchestrates the normalization, validation, and processing of raw company data
 * into clean, validated objects ready for domain entity creation.
 */
@Service
public class CompanyEnrichmentService {

    private final DataNormalizer dataNormalizer;
    private final EmailFilter emailFilter;
    private final ContactProcessor contactProcessor;

    public CompanyEnrichmentService(
        DataNormalizer dataNormalizer,
        EmailFilter emailFilter,
        ContactProcessor contactProcessor) {
        this.dataNormalizer = dataNormalizer;
        this.emailFilter = emailFilter;
        this.contactProcessor = contactProcessor;
    }

    /**
     * Enriches raw company data into clean, validated result.
     */
    public EnrichmentResult enrichCompanyData(EnrichmentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("EnrichmentRequest cannot be null");
        }

        try {
            // 1. Normalize company fields
            String normalizedName = dataNormalizer.normalizeCompanyName(request.companyName());
            String cleanDescription = dataNormalizer.normalizeDescription(request.description());
            String normalizedPhone = dataNormalizer.normalizePhone(request.phone());
            String standardizedIndustry = dataNormalizer.standardizeIndustry(request.industry());
            CompanySize size = dataNormalizer.mapCompanySize(request.size());

            // 2. Process website
            Website website = processWebsite(request.website());

            // 3. Filter and validate emails
            List<ValidatedContact> validatedContacts = emailFilter.filterAndValidateEmails(request.emails());

            // 4. Calculate quality metrics
            EnrichmentQuality quality = calculateQuality(request, validatedContacts, normalizedName, website);

            // 5. Return enriched result
            return new EnrichmentResult(
                normalizedName,
                cleanDescription,
                validatedContacts,
                normalizedPhone,
                request.technologies(), // Technologies passed through as-is
                standardizedIndustry,
                size,
                request.recentNews(),   // News passed through as-is
                website,
                quality
            );

        } catch (Exception e) {
            // Log error and return degraded result
            return createDegradedResult(request, e);
        }
    }

    /**
     * Processes website URL into Website value object.
     */
    private Website processWebsite(String websiteUrl) {
        if (websiteUrl == null || websiteUrl.trim().isEmpty()) {
            return null;
        }

        try {
            return Website.of(websiteUrl.trim());
        } catch (IllegalArgumentException e) {
            // Invalid website URL - return null
            return null;
        }
    }

    /**
     * Calculates overall enrichment quality.
     */
    private EnrichmentQuality calculateQuality(
        EnrichmentRequest request,
        List<ValidatedContact> validatedContacts,
        String normalizedName,
        Website website) {

        // Count enriched fields
        int fieldsEnriched = 0;
        int totalFields = 6; // name, description, website, industry, size, contacts

        if (normalizedName != null && !normalizedName.trim().isEmpty()) fieldsEnriched++;
        if (request.description() != null && !request.description().trim().isEmpty()) fieldsEnriched++;
        if (website != null) fieldsEnriched++;
        if (request.industry() != null && !request.industry().trim().isEmpty()) fieldsEnriched++;
        if (request.size() != null && !request.size().trim().isEmpty()) fieldsEnriched++;
        if (!validatedContacts.isEmpty()) fieldsEnriched++;

        // Calculate email metrics
        int totalEmails = request.emails() != null ? request.emails().size() : 0;
        int validEmails = validatedContacts.size();

        int corporateEmails = (int) validatedContacts.stream()
            .filter(c -> c.type() == ValidatedContact.ContactType.CORPORATE)
            .count();

        int roleBasedEmails = (int) validatedContacts.stream()
            .filter(c -> c.type() == ValidatedContact.ContactType.ROLE_BASED)
            .count();

        int personalEmails = (int) validatedContacts.stream()
            .filter(c -> c.type() == ValidatedContact.ContactType.PERSONAL)
            .count();

        int invalidEmails = totalEmails - validEmails;

        return EnrichmentQuality.calculate(
            totalEmails,
            validEmails,
            corporateEmails,
            roleBasedEmails,
            personalEmails,
            invalidEmails,
            fieldsEnriched,
            totalFields
        );
    }

    /**
     * Creates a degraded result when enrichment fails.
     */
    private EnrichmentResult createDegradedResult(EnrichmentRequest request, Exception error) {
        // Return minimal result with original data where possible
        String fallbackName = request.companyName() != null ? request.companyName().trim() : "Unknown Company";

        Website fallbackWebsite = null;
        try {
            if (request.website() != null && !request.website().trim().isEmpty()) {
                fallbackWebsite = Website.of(request.website());
            }
        } catch (Exception e) {
            // Ignore website errors in degraded mode
        }

        // Create minimal quality metrics
        EnrichmentQuality degradedQuality = EnrichmentQuality.calculate(
            request.emails() != null ? request.emails().size() : 0,
            0, 0, 0, 0,
            request.emails() != null ? request.emails().size() : 0,
            fallbackWebsite != null ? 1 : 0,
            6
        );

        return new EnrichmentResult(
            fallbackName,
            request.description(),
            List.of(), // No validated contacts in degraded mode
            request.phone(),
            request.technologies(),
            request.industry() != null ? request.industry() : "Other",
            null, // No size mapping in degraded mode
            request.recentNews(),
            fallbackWebsite,
            degradedQuality
        );
    }

    /**
     * Convenience method to get only priority contacts from enrichment result.
     */
    public List<ValidatedContact> getPriorityContacts(EnrichmentResult result) {
        return emailFilter.getPriorityContacts(result.validatedContacts());
    }

    /**
     * Convenience method to get usable contacts from enrichment result.
     */
    public List<ValidatedContact> getUsableContacts(EnrichmentResult result) {
        return emailFilter.getUsableContacts(result.validatedContacts());
    }

    /**
     * Validates if enrichment result is sufficient for creating a Company.
     */
    public boolean isEnrichmentSufficient(EnrichmentResult result) {
        return result.normalizedCompanyName() != null &&
               !result.normalizedCompanyName().trim().isEmpty() &&
               result.website() != null &&
               result.hasValidContacts() &&
               result.quality().completenessScore() >= 0.5;
    }
}