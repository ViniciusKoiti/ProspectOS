package dev.prospectos.core.enrichment;

import dev.prospectos.core.domain.Email;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class EmailFilter {

    private final EmailContactClassifier classifier = new EmailContactClassifier();
    private final EmailQualityCalculator qualityCalculator = new EmailQualityCalculator();

    public List<ValidatedContact> filterAndValidateEmails(List<String> rawEmails) {
        if (rawEmails == null || rawEmails.isEmpty()) {
            return List.of();
        }
        List<ValidatedContact> results = new ArrayList<>();
        Set<String> seenAddresses = new HashSet<>();
        for (String rawEmail : rawEmails) {
            if (rawEmail == null || rawEmail.trim().isEmpty()) {
                continue;
            }
            try {
                Email email = Email.of(rawEmail.trim());
                if (!seenAddresses.add(email.getAddress())) {
                    continue;
                }
                var type = classifier.classify(email);
                results.add(new ValidatedContact(email, type, classifier.statusFor(type)));
            } catch (IllegalArgumentException ignored) {
                // Ignore invalid emails
            }
        }
        return results;
    }

    public List<ValidatedContact> getPriorityContacts(List<ValidatedContact> validatedContacts) {
        return validatedContacts.stream().filter(ValidatedContact::isPriority).toList();
    }

    public List<ValidatedContact> getUsableContacts(List<ValidatedContact> validatedContacts) {
        return validatedContacts.stream().filter(ValidatedContact::isUsable).toList();
    }

    public EnrichmentQuality calculateEmailQuality(List<String> rawEmails, List<ValidatedContact> validatedContacts) {
        return qualityCalculator.calculate(rawEmails, validatedContacts);
    }
}
