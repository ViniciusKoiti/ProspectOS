package dev.prospectos.infrastructure.service.prospect;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dev.prospectos.api.dto.ProspectContactResponse;
import dev.prospectos.core.domain.Contact;
import org.springframework.stereotype.Component;

@Component
public final class ProspectContactAssembler {

    List<ProspectContactResponse> merge(List<Contact> internalContacts, List<ProspectContactResponse> hunterContacts) {
        Map<String, ProspectContactResponse> contacts = new LinkedHashMap<>();
        hunterContacts.forEach(contact -> putIfValid(contacts, contact));
        internalContacts.stream().map(this::fromInternal).forEach(contact -> putIfValid(contacts, contact));
        return List.copyOf(contacts.values());
    }

    private ProspectContactResponse fromInternal(Contact contact) {
        return new ProspectContactResponse(
            contact.getEmail() != null ? contact.getEmail().getAddress() : null,
            contact.getName(),
            contact.getPosition(),
            null,
            "internal"
        );
    }

    private void putIfValid(Map<String, ProspectContactResponse> contacts, ProspectContactResponse contact) {
        if (contact.email() == null || contact.email().isBlank()) {
            return;
        }
        contacts.putIfAbsent(contact.email().trim().toLowerCase(), contact);
    }
}
