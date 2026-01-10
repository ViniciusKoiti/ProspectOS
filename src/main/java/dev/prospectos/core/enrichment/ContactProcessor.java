package dev.prospectos.core.enrichment;

import dev.prospectos.core.domain.Company.Contact;
import dev.prospectos.core.enrichment.ValidatedContact.ContactType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Processes validated contacts and converts them to domain Contact objects.
 */
@Component
public class ContactProcessor {

    /**
     * Converts validated contacts to domain Contact objects.
     * Only processes usable contacts (valid or flagged).
     */
    public List<Contact> processContacts(List<ValidatedContact> validatedContacts) {
        if (validatedContacts == null || validatedContacts.isEmpty()) {
            return List.of();
        }

        return validatedContacts.stream()
            .filter(ValidatedContact::isUsable)
            .map(this::convertToContact)
            .collect(Collectors.toList());
    }

    /**
     * Converts a ValidatedContact to a domain Contact object.
     */
    private Contact convertToContact(ValidatedContact validatedContact) {
        String name = generateContactName(validatedContact);
        String position = generatePosition(validatedContact);

        return new Contact(
            name,
            validatedContact.email(),
            position,
            null // phoneNumber - not available in email-only processing
        );
    }

    /**
     * Generates a contact name based on the email address.
     * This is a fallback when no name is provided in the raw data.
     */
    private String generateContactName(ValidatedContact validatedContact) {
        String localPart = validatedContact.email().getLocalPart();

        // For role-based emails, use the role as the name
        if (validatedContact.type() == ContactType.ROLE_BASED ||
            validatedContact.type() == ContactType.SUPPORT) {
            return capitalizeRole(localPart);
        }

        // For personal/corporate emails, try to extract a name
        if (localPart.contains(".")) {
            // Handle formats like "john.doe" or "j.smith"
            String[] parts = localPart.split("\\.");
            if (parts.length == 2) {
                return capitalizeWord(parts[0]) + " " + capitalizeWord(parts[1]);
            }
        }

        // For other formats, just capitalize the local part
        return capitalizeWord(localPart);
    }

    /**
     * Generates a position/title based on the contact type.
     */
    private String generatePosition(ValidatedContact validatedContact) {
        return switch (validatedContact.type()) {
            case ROLE_BASED -> "Contact";
            case SUPPORT -> "Support";
            case PERSONAL -> "Contact";
            case CORPORATE -> "Employee";
        };
    }

    /**
     * Capitalizes a word properly.
     */
    private String capitalizeWord(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }

        // Remove numbers and special characters for name generation
        word = word.replaceAll("[^a-zA-Z]", "");

        if (word.isEmpty()) {
            return "Contact";
        }

        return Character.toUpperCase(word.charAt(0)) +
               (word.length() > 1 ? word.substring(1).toLowerCase() : "");
    }

    /**
     * Capitalizes role-based email prefixes properly.
     */
    private String capitalizeRole(String role) {
        return switch (role.toLowerCase()) {
            case "info" -> "Information";
            case "contact" -> "Contact";
            case "admin", "administrator" -> "Administrator";
            case "support" -> "Support";
            case "help" -> "Help Desk";
            case "sales" -> "Sales";
            case "marketing" -> "Marketing";
            case "hr" -> "Human Resources";
            case "jobs", "careers" -> "Careers";
            case "legal" -> "Legal";
            case "billing" -> "Billing";
            case "customerservice", "customer-service" -> "Customer Service";
            default -> capitalizeWord(role);
        };
    }

    /**
     * Filters contacts to get only priority ones (corporate, valid emails).
     */
    public List<Contact> getPriorityContacts(List<ValidatedContact> validatedContacts) {
        return validatedContacts.stream()
            .filter(ValidatedContact::isPriority)
            .map(this::convertToContact)
            .collect(Collectors.toList());
    }

    /**
     * Gets statistics about contact processing.
     */
    public ContactProcessingStats getProcessingStats(List<ValidatedContact> validatedContacts, List<Contact> processedContacts) {
        int totalValidated = validatedContacts.size();
        int processed = processedContacts.size();

        long corporateContacts = validatedContacts.stream()
            .filter(c -> c.type() == ContactType.CORPORATE)
            .count();

        long roleBasedContacts = validatedContacts.stream()
            .filter(c -> c.type() == ContactType.ROLE_BASED)
            .count();

        return new ContactProcessingStats(
            totalValidated,
            processed,
            (int) corporateContacts,
            (int) roleBasedContacts
        );
    }

    /**
     * Statistics about contact processing.
     */
    public record ContactProcessingStats(
        int totalValidatedContacts,
        int processedContacts,
        int corporateContacts,
        int roleBasedContacts
    ) {
        public double getProcessingRate() {
            return totalValidatedContacts > 0 ? (double) processedContacts / totalValidatedContacts : 0.0;
        }

        public boolean hasGoodQuality() {
            return corporateContacts > 0 && getProcessingRate() >= 0.8;
        }
    }
}