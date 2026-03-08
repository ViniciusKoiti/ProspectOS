package dev.prospectos.core.enrichment;

import dev.prospectos.core.domain.Company.Contact;
import dev.prospectos.core.domain.Email;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ContactProcessorTest {

    private final ContactProcessor processor = new ContactProcessor();

    @Test
    void processContactsFiltersInvalidAndBuildsNamesAndPositions() {
        List<Contact> contacts = processor.processContacts(List.of(
            ValidatedContact.valid(Email.of("john.doe@acme.com"), ValidatedContact.ContactType.CORPORATE),
            ValidatedContact.flagged(Email.of("info@acme.com"), ValidatedContact.ContactType.ROLE_BASED),
            new ValidatedContact(Email.of("invalid@acme.com"), ValidatedContact.ContactType.CORPORATE, ValidatedContact.ValidationStatus.INVALID)
        ));

        assertThat(contacts).hasSize(2);
        assertThat(contacts.get(0).getName()).isEqualTo("John Doe");
        assertThat(contacts.get(0).getPosition()).isEqualTo("Employee");
        assertThat(contacts.get(1).getName()).isEqualTo("Information");
        assertThat(contacts.get(1).getPosition()).isEqualTo("Contact");
    }

    @Test
    void processContactsHandlesNullAndFallbackNames() {
        assertThat(processor.processContacts(null)).isEmpty();
        assertThat(processor.processContacts(List.of())).isEmpty();

        List<Contact> contacts = processor.processContacts(List.of(
            ValidatedContact.valid(Email.of("1234@acme.com"), ValidatedContact.ContactType.PERSONAL),
            ValidatedContact.valid(Email.of("support@acme.com"), ValidatedContact.ContactType.SUPPORT)
        ));

        assertThat(contacts.get(0).getName()).isEqualTo("Contact");
        assertThat(contacts.get(1).getName()).isEqualTo("Support");
        assertThat(contacts.get(1).getPosition()).isEqualTo("Support");
    }

    @Test
    void getPriorityContactsAndStatsReflectQuality() {
        List<ValidatedContact> validatedContacts = List.of(
            ValidatedContact.valid(Email.of("jane@acme.com"), ValidatedContact.ContactType.CORPORATE),
            ValidatedContact.flagged(Email.of("sales@acme.com"), ValidatedContact.ContactType.ROLE_BASED)
        );

        List<Contact> priorityContacts = processor.getPriorityContacts(validatedContacts);
        ContactProcessor.ContactProcessingStats stats = processor.getProcessingStats(
            validatedContacts,
            processor.processContacts(validatedContacts)
        );

        assertThat(priorityContacts).singleElement().satisfies(contact ->
            assertThat(contact.getEmail().getAddress()).isEqualTo("jane@acme.com")
        );
        assertThat(stats.getProcessingRate()).isEqualTo(1.0);
        assertThat(stats.hasGoodQuality()).isTrue();
    }

    @Test
    void processContactsMapsHyphenatedRoleNames() {
        List<Contact> contacts = processor.processContacts(List.of(
            ValidatedContact.valid(Email.of("customer-service@acme.com"), ValidatedContact.ContactType.ROLE_BASED)
        ));

        assertThat(contacts).singleElement().satisfies(contact -> {
            assertThat(contact.getName()).isEqualTo("Customer Service");
            assertThat(contact.getPosition()).isEqualTo("Contact");
        });
    }
}
