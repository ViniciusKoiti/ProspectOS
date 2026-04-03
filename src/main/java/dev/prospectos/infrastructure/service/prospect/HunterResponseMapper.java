package dev.prospectos.infrastructure.service.prospect;

import java.util.Comparator;
import java.util.List;

import dev.prospectos.api.dto.ProspectContactResponse;

final class HunterResponseMapper {

    List<ProspectContactResponse> toContacts(HunterDomainSearchResponse response, int limit) {
        if (response == null || response.data() == null || response.data().emails() == null) {
            return List.of();
        }
        return response.data().emails().stream()
            .filter(entry -> entry.value() != null && !entry.value().isBlank())
            .sorted(Comparator.comparing(HunterEmailEntry::confidence, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(limit)
            .map(this::toContact)
            .toList();
    }

    private ProspectContactResponse toContact(HunterEmailEntry entry) {
        return new ProspectContactResponse(
            entry.value().trim(),
            fullName(entry.firstName(), entry.lastName()),
            blankToNull(entry.position()),
            entry.confidence(),
            "hunter"
        );
    }

    private String fullName(String firstName, String lastName) {
        String fullName = (blankToEmpty(firstName) + " " + blankToEmpty(lastName)).trim();
        return fullName.isEmpty() ? null : fullName;
    }

    private String blankToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
