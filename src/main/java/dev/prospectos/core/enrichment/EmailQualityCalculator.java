package dev.prospectos.core.enrichment;

import dev.prospectos.core.enrichment.ValidatedContact.ContactType;

import java.util.List;

final class EmailQualityCalculator {

    EnrichmentQuality calculate(List<String> rawEmails, List<ValidatedContact> validatedContacts) {
        int totalEmails = rawEmails != null ? rawEmails.size() : 0;
        int validEmails = validatedContacts.size();
        int corporateEmails = countByType(validatedContacts, ContactType.CORPORATE);
        int roleBasedEmails = countByType(validatedContacts, ContactType.ROLE_BASED);
        int personalEmails = countByType(validatedContacts, ContactType.PERSONAL);
        int invalidEmails = totalEmails - validEmails;
        double completeness = totalEmails > 0 ? (double) validEmails / totalEmails : 0.0;
        return new EnrichmentQuality(
            totalEmails,
            validEmails,
            corporateEmails,
            roleBasedEmails,
            personalEmails,
            invalidEmails,
            completeness
        );
    }

    private int countByType(List<ValidatedContact> contacts, ContactType type) {
        return (int) contacts.stream().filter(c -> c.type() == type).count();
    }
}
