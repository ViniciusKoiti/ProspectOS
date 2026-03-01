package dev.prospectos.ai.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UrlNormalizationService.
 * Tests URL normalization logic to ensure consistent behavior.
 */
class UrlNormalizationServiceTest {

    private UrlNormalizationService urlNormalizationService;

    @BeforeEach
    void setUp() {
        urlNormalizationService = new UrlNormalizationService();
    }

    @Test
    @DisplayName("Should use default URL when input is null")
    void shouldUseDefaultUrlWhenNull() {
        String result = urlNormalizationService.normalizeGroqUrl(null);
        assertEquals("https://api.groq.com/openai/v1", result);
    }

    @Test
    @DisplayName("Should use default URL when input is empty")
    void shouldUseDefaultUrlWhenEmpty() {
        String result = urlNormalizationService.normalizeGroqUrl("");
        assertEquals("https://api.groq.com/openai/v1", result);
    }

    @Test
    @DisplayName("Should use default URL when input is blank")
    void shouldUseDefaultUrlWhenBlank() {
        String result = urlNormalizationService.normalizeGroqUrl("   ");
        assertEquals("https://api.groq.com/openai/v1", result);
    }

    @Test
    @DisplayName("Should append /v1 when missing")
    void shouldAppendV1WhenMissing() {
        String input = "https://api.groq.com/openai";
        String result = urlNormalizationService.normalizeGroqUrl(input);
        assertEquals("https://api.groq.com/openai/v1", result);
    }

    @Test
    @DisplayName("Should not duplicate /v1 when already present")
    void shouldNotDuplicateV1WhenPresent() {
        String input = "https://api.groq.com/openai/v1";
        String result = urlNormalizationService.normalizeGroqUrl(input);
        assertEquals("https://api.groq.com/openai/v1", result);
    }

    @Test
    @DisplayName("Should remove trailing slashes before adding /v1")
    void shouldRemoveTrailingSlashesBeforeAddingV1() {
        String input = "https://api.groq.com/openai/";
        String result = urlNormalizationService.normalizeGroqUrl(input);
        assertEquals("https://api.groq.com/openai/v1", result);
    }

    @Test
    @DisplayName("Should remove multiple trailing slashes")
    void shouldRemoveMultipleTrailingSlashes() {
        String input = "https://api.groq.com/openai///";
        String result = urlNormalizationService.normalizeGroqUrl(input);
        assertEquals("https://api.groq.com/openai/v1", result);
    }

    @Test
    @DisplayName("Should handle URL with trailing slash and existing /v1")
    void shouldHandleTrailingSlashWithExistingV1() {
        String input = "https://api.groq.com/openai/v1/";
        String result = urlNormalizationService.normalizeGroqUrl(input);
        assertEquals("https://api.groq.com/openai/v1", result);
    }

    @Test
    @DisplayName("Should trim whitespace from input")
    void shouldTrimWhitespaceFromInput() {
        String input = "  https://api.groq.com/openai  ";
        String result = urlNormalizationService.normalizeGroqUrl(input);
        assertEquals("https://api.groq.com/openai/v1", result);
    }

    @Test
    @DisplayName("Should handle custom domain correctly")
    void shouldHandleCustomDomainCorrectly() {
        String input = "https://custom.groq.domain.com/api";
        String result = urlNormalizationService.normalizeGroqUrl(input);
        assertEquals("https://custom.groq.domain.com/api/v1", result);
    }

    @Test
    @DisplayName("Should handle localhost URLs")
    void shouldHandleLocalhostUrls() {
        String input = "http://localhost:8080/groq-proxy";
        String result = urlNormalizationService.normalizeGroqUrl(input);
        assertEquals("http://localhost:8080/groq-proxy/v1", result);
    }

    // Tests for URL validation
    @ParameterizedTest
    @DisplayName("Should validate valid HTTP/HTTPS URLs")
    @ValueSource(strings = {
        "https://api.groq.com",
        "http://localhost:8080",
        "https://custom.domain.com/path",
        "http://127.0.0.1:3000"
    })
    void shouldValidateValidUrls(String url) {
        assertTrue(urlNormalizationService.isValidUrl(url));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Should reject null or empty URLs")
    void shouldRejectNullOrEmptyUrls(String url) {
        assertFalse(urlNormalizationService.isValidUrl(url));
    }

    @ParameterizedTest
    @DisplayName("Should reject invalid URL formats")
    @ValueSource(strings = {
        "ftp://invalid.protocol.com",
        "just-a-string",
        "www.missing-protocol.com",
        "://missing-scheme.com"
    })
    void shouldRejectInvalidUrls(String url) {
        assertFalse(urlNormalizationService.isValidUrl(url));
    }

    @Test
    @DisplayName("Should reject blank URLs")
    void shouldRejectBlankUrls() {
        assertFalse(urlNormalizationService.isValidUrl("   "));
    }

    // Edge cases
    @Test
    @DisplayName("Should handle URL with port correctly")
    void shouldHandleUrlWithPort() {
        String input = "https://api.groq.com:443/openai";
        String result = urlNormalizationService.normalizeGroqUrl(input);
        assertEquals("https://api.groq.com:443/openai/v1", result);
    }

    @Test
    @DisplayName("Should handle URL with query parameters")
    void shouldHandleUrlWithQueryParameters() {
        String input = "https://api.groq.com/openai?param=value";
        String result = urlNormalizationService.normalizeGroqUrl(input);
        assertEquals("https://api.groq.com/openai/v1?param=value", result);
    }

    @Test
    @DisplayName("Should handle URL with query parameters and existing /v1")
    void shouldHandleUrlWithQueryParametersAndExistingV1() {
        String input = "https://api.groq.com/openai/v1?param=value";
        String result = urlNormalizationService.normalizeGroqUrl(input);
        assertEquals("https://api.groq.com/openai/v1?param=value", result);
    }
}