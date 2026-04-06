package dev.prospectos.infrastructure.service.prospect;

import dev.prospectos.api.dto.ProspectContactResponse;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public final class HunterResponseMapper {

    List<ProspectContactResponse> toContacts(HunterDomainSearchResponse response, int maxResults) {
        if (response == null || response.data() == null || response.data().emails() == null) {
            return List.of();
        }
        return response.data().emails().stream()
            .filter(email -> email != null && email.value() != null && !email.value().isBlank())
            .sorted(Comparator.comparing(HunterEmailEntry::confidence, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(Math.max(0, maxResults))
            .map(this::toContact)
            .toList();
    }

    private ProspectContactResponse toContact(HunterEmailEntry email) {
        return new ProspectContactResponse(
            email.value(),
            fullName(email),
            email.position(),
            email.confidence(),
            "hunter"
        );
    }

    private String fullName(HunterEmailEntry email) {
        String firstName = email.firstName() == null ? "" : email.firstName().trim();
        String lastName = email.lastName() == null ? "" : email.lastName().trim();
        String value = (firstName + " " + lastName).trim();
        return value.isEmpty() ? null : value;
    }
}
