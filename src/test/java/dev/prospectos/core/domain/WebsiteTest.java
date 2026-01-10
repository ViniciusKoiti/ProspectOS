package dev.prospectos.core.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebsiteTest {

    @Test
    void ofRejectsNullOrBlank() {
        assertThrows(IllegalArgumentException.class, () -> Website.of(null));
        assertThrows(IllegalArgumentException.class, () -> Website.of(" "));
    }

    @Test
    void ofNormalizesMissingScheme() {
        Website website = Website.of("example.com");

        assertEquals("https://example.com", website.getUrl());
        assertEquals("example.com", website.getDomain());
        assertTrue(website.isSecure());
    }

    @Test
    void ofKeepsHttpScheme() {
        Website website = Website.of("http://example.com");

        assertEquals("http://example.com", website.getUrl());
        assertEquals("example.com", website.getDomain());
        assertFalse(website.isSecure());
    }

    @Test
    void ofRejectsInvalidUrl() {
        assertThrows(IllegalArgumentException.class, () -> Website.of("http://exa mple.com"));
    }
}
