package dev.prospectos.core.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompanyNestedTypesTest {

    @Test
    void contactValidatesNameAndExposesFields() {
        Email email = Email.of("jane@acme.com");
        Contact contact = new Contact(" Jane Doe ", email, "CTO", "123");

        assertThat(contact.getName()).isEqualTo("Jane Doe");
        assertThat(contact.getEmail()).isEqualTo(email);
        assertThat(contact.getPosition()).isEqualTo("CTO");
        assertThat(contact.getPhoneNumber()).isEqualTo("123");
        assertThat(contact.hasValidEmail()).isTrue();
    }

    @Test
    void contactRejectsBlankNameAndSupportsNullEmail() {
        assertThatThrownBy(() -> new Contact(" ", Email.of("jane@acme.com"), "CTO", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Contact name cannot be null or empty");

        Contact contact = new Contact("No Email", null, "CTO", null);
        assertThat(contact.hasValidEmail()).isFalse();
    }

    @Test
    void technologySignalExposesFields() {
        Instant detectedAt = Instant.now();
        TechnologySignal signal = new TechnologySignal("Kubernetes", "Using k8s", detectedAt);

        assertThat(signal.getTechnology()).isEqualTo("Kubernetes");
        assertThat(signal.getDescription()).isEqualTo("Using k8s");
        assertThat(signal.getDetectedAt()).isEqualTo(detectedAt);
    }

    @Test
    void technologySignalJpaConstructorInitializesNullState() {
        TechnologySignal signal = new TechnologySignal();

        assertThat(signal.getTechnology()).isNull();
        assertThat(signal.getDescription()).isNull();
        assertThat(signal.getDetectedAt()).isNull();
    }
}

