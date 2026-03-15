package dev.prospectos.infrastructure.service.inmemory;

import dev.prospectos.api.dto.CompanyContactDTO;
import dev.prospectos.core.domain.Email;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

final class InMemoryCompanyContactSupport {

    List<CompanyContactDTO> findContacts(InMemoryCoreDataStore store, Long companyId) {
        return List.copyOf(store.companyContacts().getOrDefault(companyId, List.of()));
    }

    boolean addValidUniqueEmails(InMemoryCoreDataStore store, Long companyId, List<String> emails) {
        List<CompanyContactDTO> contacts = new ArrayList<>(store.companyContacts().getOrDefault(companyId, List.of()));
        Set<String> existing = contacts.stream().map(CompanyContactDTO::email).filter(Objects::nonNull)
            .map(this::normalized).collect(HashSet::new, Set::add, Set::addAll);
        boolean changed = false;
        for (String candidate : emails == null ? List.<String>of() : emails) {
            Email email = parse(candidate);
            if (email == null || !existing.add(normalized(email.getAddress()))) {
                continue;
            }
            contacts.add(new CompanyContactDTO(email.getLocalPart(), email.getAddress(), null, null));
            changed = true;
        }
        if (changed) {
            store.companyContacts().put(companyId, List.copyOf(contacts));
        }
        return changed;
    }

    String primaryEmail(InMemoryCoreDataStore store, Long companyId) {
        return store.companyContacts().getOrDefault(companyId, List.of()).stream()
            .map(CompanyContactDTO::email).findFirst().orElse(null);
    }

    int contactCount(InMemoryCoreDataStore store, Long companyId) {
        return store.companyContacts().getOrDefault(companyId, List.of()).size();
    }

    private String normalized(String email) { return email.toLowerCase(Locale.ROOT).trim(); }

    private Email parse(String candidate) {
        try {
            return (candidate == null || candidate.isBlank()) ? null : Email.of(candidate);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
