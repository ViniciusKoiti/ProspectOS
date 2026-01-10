package dev.prospectos.core.enrichment;

import dev.prospectos.core.domain.Email;
import dev.prospectos.core.enrichment.ValidatedContact.ContactType;
import dev.prospectos.core.enrichment.ValidatedContact.ValidationStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles email validation, filtering, and classification.
 */
@Component
public class EmailFilter {

    private static final Set<String> ROLE_BASED_PREFIXES = Set.of(
        "info", "contact", "admin", "administrator", "support", "help",
        "noreply", "no-reply", "donotreply", "sales", "marketing",
        "hr", "jobs", "careers", "legal", "compliance", "billing",
        "accounts", "finance", "customerservice", "customer-service"
    );

    private static final Set<String> SUPPORT_PREFIXES = Set.of(
        "support", "help", "customerservice", "customer-service",
        "helpdesk", "servicedesk", "assistance"
    );

    /**
     * Filters and validates a list of raw email strings.
     */
    public List<ValidatedContact> filterAndValidateEmails(List<String> rawEmails) {
        if (rawEmails == null || rawEmails.isEmpty()) {
            return List.of();
        }

        List<ValidatedContact> results = new ArrayList<>();
        Set<String> seenAddresses = new HashSet<>();

        for (String rawEmail : rawEmails) {
            if (rawEmail == null) {
                continue;
            }

            String trimmed = rawEmail.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            try {
                Email email = Email.of(trimmed);
                if (!seenAddresses.add(email.getAddress())) {
                    continue;
                }

                ContactType type = classifyEmailType(email);
                ValidationStatus status = determineValidationStatus(email, type);
                results.add(new ValidatedContact(email, type, status));
            } catch (IllegalArgumentException e) {
                // Ignore invalid emails
            }
        }

        return results;
    }

    /**
     * Processes a single email string into a ValidatedContact.
     */
    /**
     * Classifies an email into a contact type.
     */
    private ContactType classifyEmailType(Email email) {
        String localPart = email.getLocalPart().toLowerCase();

        // Check for support emails first (most specific)
        if (SUPPORT_PREFIXES.contains(localPart)) {
            return ContactType.SUPPORT;
        }

        // Check for role-based emails
        if (isRoleBasedEmail(localPart)) {
            return ContactType.ROLE_BASED;
        }

        // Check for personal domains
        if (email.isPersonalEmail()) {
            return ContactType.PERSONAL;
        }

        // Default to corporate
        return ContactType.CORPORATE;
    }

    /**
     * Determines the validation status based on email properties.
     */
    private ValidationStatus determineValidationStatus(Email email, ContactType type) {
        // Role-based and support emails are flagged for review
        if (type == ContactType.ROLE_BASED || type == ContactType.SUPPORT) {
            return ValidationStatus.FLAGGED;
        }

        // Personal emails in corporate context might be flagged
        if (type == ContactType.PERSONAL) {
            return ValidationStatus.FLAGGED;
        }

        // Corporate emails are considered valid
        return ValidationStatus.VALID;
    }

    /**
     * Checks if an email local part indicates a role-based email.
     */
    private boolean isRoleBasedEmail(String localPart) {
        // Exact matches
        if (ROLE_BASED_PREFIXES.contains(localPart)) {
            return true;
        }

        // Check for common patterns
        return localPart.matches("^(info|contact|admin|support|sales|marketing)\\d*$") ||
               localPart.startsWith("noreply") ||
               localPart.startsWith("no-reply") ||
               localPart.startsWith("donotreply");
    }

    /**
     * Filters to only get priority contacts (corporate emails that are valid).
     */
    public List<ValidatedContact> getPriorityContacts(List<ValidatedContact> validatedContacts) {
        return validatedContacts.stream()
            .filter(ValidatedContact::isPriority)
            .collect(Collectors.toList());
    }

    /**
     * Filters to get all usable contacts (valid or flagged).
     */
    public List<ValidatedContact> getUsableContacts(List<ValidatedContact> validatedContacts) {
        return validatedContacts.stream()
            .filter(ValidatedContact::isUsable)
            .collect(Collectors.toList());
    }

    /**
     * Creates enrichment quality metrics for email processing.
     */
    public EnrichmentQuality calculateEmailQuality(List<String> rawEmails, List<ValidatedContact> validatedContacts) {
        int totalEmails = rawEmails != null ? rawEmails.size() : 0;
        int validEmails = validatedContacts.size();

        int corporateEmails = (int) validatedContacts.stream()
            .filter(c -> c.type() == ContactType.CORPORATE)
            .count();

        int roleBasedEmails = (int) validatedContacts.stream()
            .filter(c -> c.type() == ContactType.ROLE_BASED)
            .count();

        int personalEmails = (int) validatedContacts.stream()
            .filter(c -> c.type() == ContactType.PERSONAL)
            .count();

        int invalidEmails = totalEmails - validEmails;

        // Calculate completeness score (simplified for email processing)
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
}
