package dev.prospectos.core.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailTest {

    @Test
    void ofRejectsNullOrBlank() {
        assertThrows(IllegalArgumentException.class, () -> Email.of(null));
        assertThrows(IllegalArgumentException.class, () -> Email.of(" "));
    }

    @Test
    void ofRejectsInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> Email.of("invalid"));
        assertThrows(IllegalArgumentException.class, () -> Email.of("user@"));
    }

    @Test
    void ofNormalizesAndParsesParts() {
        Email email = Email.of("User@Example.com");

        assertEquals("user@example.com", email.getAddress());
        assertEquals("user", email.getLocalPart());
        assertEquals("example.com", email.getDomain());
    }

    @Test
    void detectsPersonalEmails() {
        Email personal = Email.of("someone@gmail.com");
        Email corporate = Email.of("dev@prospectos.dev");

        assertTrue(personal.isPersonalEmail());
        assertFalse(personal.isCorporateEmail());
        assertFalse(corporate.isPersonalEmail());
        assertTrue(corporate.isCorporateEmail());
    }
}
