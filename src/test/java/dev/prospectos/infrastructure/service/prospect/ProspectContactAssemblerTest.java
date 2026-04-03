package dev.prospectos.infrastructure.service.prospect;

import java.util.List;

import dev.prospectos.api.dto.ProspectContactResponse;
import dev.prospectos.core.domain.Contact;
import dev.prospectos.core.domain.Email;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProspectContactAssemblerTest {

    private final ProspectContactAssembler assembler = new ProspectContactAssembler();

    @Test
    void mergesHunterContactsBeforeInternalContacts() {
        var internalContact = new Contact("Internal Owner", Email.of("owner@acme.com"), "CEO", null);
        var hunterContact = new ProspectContactResponse("owner@acme.com", "Hunter Owner", "Founder", 88, "hunter");
        var fallbackContact = new ProspectContactResponse("hello@acme.com", "Hello Team", null, 71, "hunter");

        var contacts = assembler.merge(List.of(internalContact), List.of(hunterContact, fallbackContact));

        assertThat(contacts).hasSize(2);
        assertThat(contacts.getFirst().source()).isEqualTo("hunter");
        assertThat(contacts.getFirst().email()).isEqualTo("owner@acme.com");
        assertThat(contacts.get(1).email()).isEqualTo("hello@acme.com");
    }
}
