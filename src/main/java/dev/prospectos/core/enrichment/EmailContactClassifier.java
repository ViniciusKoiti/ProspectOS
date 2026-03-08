package dev.prospectos.core.enrichment;

import dev.prospectos.core.domain.Email;
import dev.prospectos.core.enrichment.ValidatedContact.ContactType;
import dev.prospectos.core.enrichment.ValidatedContact.ValidationStatus;

import java.util.Set;

final class EmailContactClassifier {

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

    ContactType classify(Email email) {
        String localPart = email.getLocalPart().toLowerCase();
        if (SUPPORT_PREFIXES.contains(localPart)) {
            return ContactType.SUPPORT;
        }
        if (isRoleBasedEmail(localPart)) {
            return ContactType.ROLE_BASED;
        }
        if (email.isPersonalEmail()) {
            return ContactType.PERSONAL;
        }
        return ContactType.CORPORATE;
    }

    ValidationStatus statusFor(ContactType type) {
        if (type == ContactType.CORPORATE) {
            return ValidationStatus.VALID;
        }
        return ValidationStatus.FLAGGED;
    }

    private boolean isRoleBasedEmail(String localPart) {
        if (ROLE_BASED_PREFIXES.contains(localPart)) {
            return true;
        }
        return localPart.matches("^(info|contact|admin|support|sales|marketing)\\d*$")
            || localPart.startsWith("noreply")
            || localPart.startsWith("no-reply")
            || localPart.startsWith("donotreply");
    }
}
