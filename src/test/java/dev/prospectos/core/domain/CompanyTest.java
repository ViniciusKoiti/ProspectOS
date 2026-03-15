package dev.prospectos.core.domain;

import dev.prospectos.core.domain.events.SignalDetected;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompanyTest {

    @Test
    void createSetsDefaults() {
        Company company = Company.create("ProspectOS", Website.of("prospectos.dev"), "Software");

        assertEquals(ProspectingStatus.NEW, company.getStatus());
        assertEquals(0.0, company.getProspectingScore().getDoubleValue());
        assertFalse(company.hasActiveSignals());
        assertTrue(ExternalIdPolicy.isSafe(company.getExternalId()));
    }

    @Test
    void updateScoreHighMovesToReviewing() {
        Company company = Company.create("ProspectOS", Website.of("prospectos.dev"), "Software");

        company.updateScore(Score.of(80), "test");

        assertEquals(ProspectingStatus.REVIEWING, company.getStatus());
    }

    @Test
    void qualifyOnlyAllowedFromValidStates() {
        Company company = Company.create("ProspectOS", Website.of("prospectos.dev"), "Software");

        company.qualify();
        assertEquals(ProspectingStatus.QUALIFIED, company.getStatus());

        company.disqualify("no fit");
        assertThrows(IllegalStateException.class, company::qualify);
    }

    @Test
    void addContactRejectsDuplicateEmails() {
        Company company = Company.create("ProspectOS", Website.of("prospectos.dev"), "Software");
        Contact contact = new Contact(
            "Alex",
            Email.of("alex@prospectos.dev"),
            "CTO",
            "+1-555-0100"
        );

        company.addContact(contact);

        Contact duplicate = new Contact(
            "Alex 2",
            Email.of("alex@prospectos.dev"),
            "CTO",
            "+1-555-0200"
        );

        assertThrows(IllegalArgumentException.class, () -> company.addContact(duplicate));
        assertTrue(company.hasValidContactInfo());
    }

    @Test
    void detectTechnologySignalAddsSignal() {
        Company company = Company.create("ProspectOS", Website.of("prospectos.dev"), "Software");

        company.detectSignal(
            SignalDetected.SignalType.TECHNOLOGY_ADOPTION,
            "Uses Kubernetes",
            Map.of("technology", "Kubernetes")
        );

        assertTrue(company.hasActiveSignals());
        assertEquals(1, company.getTechnologySignals().size());
        assertEquals("Kubernetes", company.getTechnologySignals().getFirst().getTechnology());
    }
}
