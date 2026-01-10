package dev.prospectos.core.enrichment;

import dev.prospectos.core.domain.Email;

/**
 * Represents a validated contact with enrichment metadata.
 */
public record ValidatedContact(
    Email email,                // Valid Email value object
    ContactType type,          // CORPORATE, PERSONAL, ROLE_BASED
    ValidationStatus status    // VALID, INVALID, FLAGGED
) {

    public enum ContactType {
        CORPORATE,      // Regular corporate email (john@company.com)
        PERSONAL,       // Personal email domains (gmail, yahoo, etc.)
        ROLE_BASED,     // Generic roles (info@, noreply@, admin@)
        SUPPORT         // Support/service emails (support@, help@)
    }

    public enum ValidationStatus {
        VALID,          // Email passed all validation checks
        INVALID,        // Email failed syntax validation
        FLAGGED         // Email valid but flagged for review (e.g., role-based)
    }

    public static ValidatedContact valid(Email email, ContactType type) {
        return new ValidatedContact(email, type, ValidationStatus.VALID);
    }

    public static ValidatedContact flagged(Email email, ContactType type) {
        return new ValidatedContact(email, type, ValidationStatus.FLAGGED);
    }

    public boolean isUsable() {
        return status == ValidationStatus.VALID || status == ValidationStatus.FLAGGED;
    }

    public boolean isPriority() {
        return status == ValidationStatus.VALID && type == ContactType.CORPORATE;
    }
}