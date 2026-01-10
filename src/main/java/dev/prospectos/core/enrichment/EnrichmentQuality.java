package dev.prospectos.core.enrichment;

/**
 * Represents the quality of enrichment process results.
 */
public record EnrichmentQuality(
    int totalEmailsProcessed,
    int validEmailsFound,
    int corporateEmailsFound,
    int roleBasedEmailsFound,
    int personalEmailsFound,
    int invalidEmailsFiltered,
    double completenessScore    // 0.0 to 1.0 based on how many fields were enriched
) {

    public static EnrichmentQuality calculate(
        int totalEmails,
        int validEmails,
        int corporateEmails,
        int roleBasedEmails,
        int personalEmails,
        int invalidEmails,
        int fieldsEnriched,
        int totalFields) {

        double completeness = totalFields > 0 ? (double) fieldsEnriched / totalFields : 0.0;

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

    public boolean isHighQuality() {
        return completenessScore >= 0.7 && corporateEmailsFound > 0;
    }

    public boolean hasValidContacts() {
        return validEmailsFound > 0;
    }

    public double getEmailValidationRate() {
        return totalEmailsProcessed > 0 ? (double) validEmailsFound / totalEmailsProcessed : 0.0;
    }
}