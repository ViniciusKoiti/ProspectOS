package dev.prospectos.core.enrichment;

import dev.prospectos.core.enrichment.ValidatedContact.ContactType;

final class ContactInfoResolver {

    String nameFor(ValidatedContact validatedContact) {
        String localPart = validatedContact.email().getLocalPart();
        if (validatedContact.type() == ContactType.ROLE_BASED || validatedContact.type() == ContactType.SUPPORT) {
            return capitalizeRole(localPart);
        }
        if (localPart.contains(".")) {
            String[] parts = localPart.split("\\.");
            if (parts.length == 2) {
                return capitalizeWord(parts[0]) + " " + capitalizeWord(parts[1]);
            }
        }
        return capitalizeWord(localPart);
    }

    String positionFor(ValidatedContact validatedContact) {
        return switch (validatedContact.type()) {
            case ROLE_BASED, PERSONAL -> "Contact";
            case SUPPORT -> "Support";
            case CORPORATE -> "Employee";
        };
    }

    private String capitalizeWord(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        word = word.replaceAll("[^a-zA-Z]", "");
        if (word.isEmpty()) {
            return "Contact";
        }
        return Character.toUpperCase(word.charAt(0)) + (word.length() > 1 ? word.substring(1).toLowerCase() : "");
    }

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
}
