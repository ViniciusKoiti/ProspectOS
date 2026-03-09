package dev.prospectos.core.enrichment;

import java.util.List;

import dev.prospectos.core.domain.Website;

final class CompanyEnrichmentQualityEvaluator {

    private static final int TOTAL_FIELDS = 6;

    EnrichmentQuality calculate(
        EnrichmentRequest request,
        List<ValidatedContact> validatedContacts,
        String normalizedName,
        Website website
    ) {
        int fieldsEnriched = countFieldsEnriched(request, validatedContacts, normalizedName, website);
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
            TOTAL_FIELDS
        );
    }

    private int countFieldsEnriched(
        EnrichmentRequest request,
        List<ValidatedContact> validatedContacts,
        String normalizedName,
        Website website
    ) {
        int fieldsEnriched = 0;
        if (normalizedName != null && !normalizedName.trim().isEmpty()) {
            fieldsEnriched++;
        }
        if (request.description() != null && !request.description().trim().isEmpty()) {
            fieldsEnriched++;
        }
        if (website != null) {
            fieldsEnriched++;
        }
        if (request.industry() != null && !request.industry().trim().isEmpty()) {
            fieldsEnriched++;
        }
        if (request.size() != null && !request.size().trim().isEmpty()) {
            fieldsEnriched++;
        }
        if (!validatedContacts.isEmpty()) {
            fieldsEnriched++;
        }
        return fieldsEnriched;
    }
}
