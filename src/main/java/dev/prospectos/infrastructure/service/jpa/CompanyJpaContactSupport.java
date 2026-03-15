package dev.prospectos.infrastructure.service.jpa;

import dev.prospectos.api.dto.CompanyContactDTO;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.Contact;
import dev.prospectos.core.domain.Email;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

final class CompanyJpaContactSupport {

    List<CompanyContactDTO> toDTOs(Company company) {
        return company.getContacts().stream()
            .map(this::toDTO)
            .toList();
    }

    boolean addValidUniqueEmails(Company company, List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return false;
        }
        Set<String> existingEmails = existingEmails(company);
        boolean changed = false;
        for (String candidate : emails) {
            Email email = parseEmail(candidate);
            if (email == null) {
                continue;
            }
            if (!existingEmails.add(normalized(email.getAddress()))) {
                continue;
            }
            company.addContact(new Contact(email.getLocalPart(), email, null, null));
            changed = true;
        }
        return changed;
    }

    private CompanyContactDTO toDTO(Contact contact) {
        String email = contact.getEmail() != null ? contact.getEmail().getAddress() : null;
        return new CompanyContactDTO(contact.getName(), email, contact.getPosition(), contact.getPhoneNumber());
    }

    private Set<String> existingEmails(Company company) {
        return company.getContacts().stream()
            .map(Contact::getEmail)
            .filter(Objects::nonNull)
            .map(Email::getAddress)
            .map(this::normalized)
            .collect(HashSet::new, Set::add, Set::addAll);
    }

    private String normalized(String email) {
        return email.toLowerCase(Locale.ROOT).trim();
    }

    private Email parseEmail(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return null;
        }
        try {
            return Email.of(candidate);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
