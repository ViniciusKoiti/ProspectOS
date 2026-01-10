package dev.prospectos.core.enrichment;

import dev.prospectos.core.enrichment.ValidatedContact.ContactType;
import dev.prospectos.core.enrichment.ValidatedContact.ValidationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmailFilterTest {

    private EmailFilter emailFilter;

    @BeforeEach
    void setUp() {
        emailFilter = new EmailFilter();
    }

    @Test
    void filterAndValidateEmails_ValidCorporateEmail_ReturnsValidContact() {
        List<String> emails = List.of("john.doe@company.com");

        List<ValidatedContact> result = emailFilter.filterAndValidateEmails(emails);

        assertEquals(1, result.size());
        ValidatedContact contact = result.get(0);
        assertEquals("john.doe@company.com", contact.email().getAddress());
        assertEquals(ContactType.CORPORATE, contact.type());
        assertEquals(ValidationStatus.VALID, contact.status());
    }

    @Test
    void filterAndValidateEmails_PersonalEmail_ReturnsFlaggedContact() {
        List<String> emails = List.of("user@gmail.com");

        List<ValidatedContact> result = emailFilter.filterAndValidateEmails(emails);

        assertEquals(1, result.size());
        ValidatedContact contact = result.get(0);
        assertEquals(ContactType.PERSONAL, contact.type());
        assertEquals(ValidationStatus.FLAGGED, contact.status());
    }

    @Test
    void filterAndValidateEmails_RoleBasedEmail_ReturnsFlaggedContact() {
        List<String> emails = Arrays.asList("info@company.com", "admin@company.com", "noreply@company.com");

        List<ValidatedContact> result = emailFilter.filterAndValidateEmails(emails);

        assertEquals(3, result.size());
        result.forEach(contact -> {
            assertEquals(ContactType.ROLE_BASED, contact.type());
            assertEquals(ValidationStatus.FLAGGED, contact.status());
        });
    }

    @Test
    void filterAndValidateEmails_SupportEmail_ReturnsSupportContact() {
        List<String> emails = List.of("support@company.com");

        List<ValidatedContact> result = emailFilter.filterAndValidateEmails(emails);

        assertEquals(1, result.size());
        ValidatedContact contact = result.get(0);
        assertEquals(ContactType.SUPPORT, contact.type());
        assertEquals(ValidationStatus.FLAGGED, contact.status());
    }

    @Test
    void filterAndValidateEmails_InvalidEmails_FiltersOut() {
        List<String> emails = Arrays.asList(
            "valid@company.com",
            "invalid-email",
            "another@",
            "@domain.com",
            "",
            "spaces in@email.com"
        );

        List<ValidatedContact> result = emailFilter.filterAndValidateEmails(emails);

        assertEquals(1, result.size());
        assertEquals("valid@company.com", result.get(0).email().getAddress());
    }

    @Test
    void filterAndValidateEmails_DuplicateEmails_RemovesDuplicates() {
        List<String> emails = Arrays.asList(
            "test@company.com",
            "test@company.com",
            "TEST@COMPANY.COM"
        );

        List<ValidatedContact> result = emailFilter.filterAndValidateEmails(emails);

        assertEquals(1, result.size());
        assertEquals("test@company.com", result.get(0).email().getAddress());
    }

    @Test
    void filterAndValidateEmails_NullOrEmptyList_ReturnsEmptyList() {
        assertTrue(emailFilter.filterAndValidateEmails(null).isEmpty());
        assertTrue(emailFilter.filterAndValidateEmails(List.of()).isEmpty());
    }

    @Test
    void filterAndValidateEmails_MixedValidInvalid_OnlyReturnsValid() {
        List<String> emails = Arrays.asList(
            "john@company.com",      // Corporate - Valid
            "user@gmail.com",        // Personal - Flagged
            "invalid-email",         // Invalid - Filtered out
            "info@company.com",      // Role-based - Flagged
            "support@company.com"    // Support - Flagged
        );

        List<ValidatedContact> result = emailFilter.filterAndValidateEmails(emails);

        assertEquals(4, result.size()); // Only valid emails, but includes flagged

        long validContacts = result.stream().filter(c -> c.status() == ValidationStatus.VALID).count();
        long flaggedContacts = result.stream().filter(c -> c.status() == ValidationStatus.FLAGGED).count();

        assertEquals(1, validContacts);  // Only corporate email is truly valid
        assertEquals(3, flaggedContacts); // Personal, role-based, and support are flagged
    }

    @Test
    void getPriorityContacts_OnlyReturnsCorporateValidEmails() {
        List<ValidatedContact> contacts = Arrays.asList(
            ValidatedContact.valid(createEmail("john@company.com"), ContactType.CORPORATE),
            ValidatedContact.flagged(createEmail("info@company.com"), ContactType.ROLE_BASED),
            ValidatedContact.flagged(createEmail("user@gmail.com"), ContactType.PERSONAL)
        );

        List<ValidatedContact> priority = emailFilter.getPriorityContacts(contacts);

        assertEquals(1, priority.size());
        assertEquals("john@company.com", priority.get(0).email().getAddress());
    }

    @Test
    void getUsableContacts_ReturnsValidAndFlagged() {
        List<ValidatedContact> contacts = Arrays.asList(
            ValidatedContact.valid(createEmail("john@company.com"), ContactType.CORPORATE),
            ValidatedContact.flagged(createEmail("info@company.com"), ContactType.ROLE_BASED)
        );

        List<ValidatedContact> usable = emailFilter.getUsableContacts(contacts);

        assertEquals(2, usable.size());
    }

    private dev.prospectos.core.domain.Email createEmail(String address) {
        return dev.prospectos.core.domain.Email.of(address);
    }
}
