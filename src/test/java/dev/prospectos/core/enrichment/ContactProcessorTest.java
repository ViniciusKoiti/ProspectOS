package dev.prospectos.core.enrichment;

import dev.prospectos.core.domain.Company.Contact;
import dev.prospectos.core.domain.Email;
import dev.prospectos.core.enrichment.ValidatedContact.ContactType;
import dev.prospectos.core.enrichment.ValidatedContact.ValidationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContactProcessorTest {

    private ContactProcessor contactProcessor;

    @BeforeEach
    void setUp() {
        contactProcessor = new ContactProcessor();
    }

    @Test
    void processContacts_FiltersOutInvalidContacts() {
        List<ValidatedContact> input = List.of(
            ValidatedContact.valid(Email.of("john.doe@company.com"), ContactType.CORPORATE),
            ValidatedContact.flagged(Email.of("info@company.com"), ContactType.ROLE_BASED),
            new ValidatedContact(Email.of("jane@company.com"), ContactType.CORPORATE, ValidationStatus.INVALID)
        );

        List<Contact> contacts = contactProcessor.processContacts(input);

        assertEquals(2, contacts.size());
        assertTrue(contacts.stream().anyMatch(contact -> "John Doe".equals(contact.getName())));
        assertTrue(contacts.stream().anyMatch(contact -> "Information".equals(contact.getName())));
    }

    @Test
    void processContacts_EmptyListReturnsEmpty() {
        assertTrue(contactProcessor.processContacts(List.of()).isEmpty());
    }

    @Test
    void getPriorityContacts_ReturnsCorporateValidOnly() {
        List<ValidatedContact> input = List.of(
            ValidatedContact.valid(Email.of("john.doe@company.com"), ContactType.CORPORATE),
            ValidatedContact.flagged(Email.of("info@company.com"), ContactType.ROLE_BASED)
        );

        List<Contact> contacts = contactProcessor.getPriorityContacts(input);

        assertEquals(1, contacts.size());
        assertEquals("John Doe", contacts.get(0).getName());
    }
}
