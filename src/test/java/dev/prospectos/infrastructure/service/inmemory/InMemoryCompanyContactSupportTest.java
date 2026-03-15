package dev.prospectos.infrastructure.service.inmemory;

import dev.prospectos.api.dto.CompanyContactDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryCompanyContactSupportTest {

    private final InMemoryCompanyContactSupport support = new InMemoryCompanyContactSupport();

    @Test
    void findContactsReturnsImmutableCopyAndDefaultsToEmpty() {
        InMemoryCoreDataStore store = new InMemoryCoreDataStore();
        Long companyId = 10L;
        store.companyContacts().put(companyId, List.of(new CompanyContactDTO("Alice", "alice@example.com", null, null)));

        List<CompanyContactDTO> contacts = support.findContacts(store, companyId);

        assertEquals(1, contacts.size());
        assertThrows(
            UnsupportedOperationException.class,
            () -> contacts.add(new CompanyContactDTO("Bob", "bob@example.com", null, null))
        );
        assertTrue(support.findContacts(store, 999L).isEmpty());
    }

    @Test
    void addValidUniqueEmailsAddsOnlyValidAndUniqueCandidates() {
        InMemoryCoreDataStore store = new InMemoryCoreDataStore();
        Long companyId = 20L;
        store.companyContacts().put(companyId, List.of(new CompanyContactDTO("Alice", "alice@example.com", null, null)));

        boolean changed = support.addValidUniqueEmails(store, companyId, List.of(
            "new@example.com",
            "NEW@example.com",
            "invalid-email",
            "",
            "   "
        ));

        List<CompanyContactDTO> contacts = support.findContacts(store, companyId);
        assertTrue(changed);
        assertEquals(2, contacts.size());
        assertEquals("alice@example.com", support.primaryEmail(store, companyId));
        assertEquals(2, support.contactCount(store, companyId));
        assertEquals("new", contacts.get(1).name());
        assertEquals("new@example.com", contacts.get(1).email());
    }

    @Test
    void addValidUniqueEmailsReturnsFalseWhenNoNewEmailIsAccepted() {
        InMemoryCoreDataStore store = new InMemoryCoreDataStore();
        Long companyId = 30L;
        store.companyContacts().put(companyId, List.of(new CompanyContactDTO("Alice", "alice@example.com", null, null)));

        boolean unchangedWithDuplicates = support.addValidUniqueEmails(
            store,
            companyId,
            List.of("ALICE@example.com", "invalid", "")
        );
        boolean unchangedWithNullInput = support.addValidUniqueEmails(store, companyId, null);

        assertFalse(unchangedWithDuplicates);
        assertFalse(unchangedWithNullInput);
        assertEquals(1, support.contactCount(store, companyId));
    }

    @Test
    void primaryEmailAndContactCountHandleUnknownCompany() {
        InMemoryCoreDataStore store = new InMemoryCoreDataStore();

        assertNull(support.primaryEmail(store, 404L));
        assertEquals(0, support.contactCount(store, 404L));
    }
}