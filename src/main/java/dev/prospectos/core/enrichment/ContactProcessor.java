package dev.prospectos.core.enrichment;

import dev.prospectos.core.domain.Company.Contact;
import dev.prospectos.core.enrichment.ValidatedContact.ContactType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContactProcessor {

    private final ContactInfoResolver infoResolver = new ContactInfoResolver();

    public List<Contact> processContacts(List<ValidatedContact> validatedContacts) {
        if (validatedContacts == null || validatedContacts.isEmpty()) {
            return List.of();
        }
        return validatedContacts.stream().filter(ValidatedContact::isUsable).map(this::convertToContact).toList();
    }

    public List<Contact> getPriorityContacts(List<ValidatedContact> validatedContacts) {
        return validatedContacts.stream().filter(ValidatedContact::isPriority).map(this::convertToContact).toList();
    }

    public ContactProcessingStats getProcessingStats(List<ValidatedContact> validatedContacts, List<Contact> processedContacts) {
        int totalValidated = validatedContacts.size();
        int processed = processedContacts.size();
        long corporateContacts = validatedContacts.stream().filter(c -> c.type() == ContactType.CORPORATE).count();
        long roleBasedContacts = validatedContacts.stream().filter(c -> c.type() == ContactType.ROLE_BASED).count();
        return new ContactProcessingStats(totalValidated, processed, (int) corporateContacts, (int) roleBasedContacts);
    }

    private Contact convertToContact(ValidatedContact validatedContact) {
        return new Contact(
            infoResolver.nameFor(validatedContact),
            validatedContact.email(),
            infoResolver.positionFor(validatedContact),
            null
        );
    }

    public record ContactProcessingStats(
        int totalValidatedContacts,
        int processedContacts,
        int corporateContacts,
        int roleBasedContacts
    ) {
        public double getProcessingRate() {
            return totalValidatedContacts > 0 ? (double) processedContacts / totalValidatedContacts : 0.0;
        }

        public boolean hasGoodQuality() {
            return corporateContacts > 0 && getProcessingRate() >= 0.8;
        }
    }
}
