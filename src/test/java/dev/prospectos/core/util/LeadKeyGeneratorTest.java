package dev.prospectos.core.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeadKeyGeneratorTest {

    @Test
    void generate_shouldProduceDeterministicKey() {
        String key1 = LeadKeyGenerator.generate("https://example.com", "in-memory");
        String key2 = LeadKeyGenerator.generate("https://example.com", "in-memory");

        assertThat(key1).isEqualTo(key2);
    }

    @Test
    void generate_shouldNormalizeDomain() {
        String key1 = LeadKeyGenerator.generate("https://example.com/path", "in-memory");
        String key2 = LeadKeyGenerator.generate("https://www.example.com", "in-memory");
        String key3 = LeadKeyGenerator.generate("http://example.com:8080", "in-memory");

        // All should normalize to example.com
        assertThat(key1).isEqualTo(key2).isEqualTo(key3);
    }

    @Test
    void generate_shouldNormalizeSource() {
        String key1 = LeadKeyGenerator.generate("https://example.com", "Apollo");
        String key2 = LeadKeyGenerator.generate("https://example.com", "apollo");
        String key3 = LeadKeyGenerator.generate("https://example.com", "  APOLLO  ");

        assertThat(key1).isEqualTo(key2).isEqualTo(key3);
    }

    @Test
    void generate_shouldProduceDifferentKeysForDifferentDomains() {
        String key1 = LeadKeyGenerator.generate("https://example.com", "in-memory");
        String key2 = LeadKeyGenerator.generate("https://other.com", "in-memory");

        assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    void generate_shouldProduceDifferentKeysForDifferentSources() {
        String key1 = LeadKeyGenerator.generate("https://example.com", "in-memory");
        String key2 = LeadKeyGenerator.generate("https://example.com", "apollo");

        assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    void generate_shouldRejectNullWebsite() {
        assertThatThrownBy(() -> LeadKeyGenerator.generate(null, "in-memory"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Website URL cannot be null or blank");
    }

    @Test
    void generate_shouldRejectBlankWebsite() {
        assertThatThrownBy(() -> LeadKeyGenerator.generate("  ", "in-memory"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Website URL cannot be null or blank");
    }

    @Test
    void generate_shouldRejectNullSource() {
        assertThatThrownBy(() -> LeadKeyGenerator.generate("https://example.com", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Source name cannot be null or blank");
    }

    @Test
    void generate_shouldRejectBlankSource() {
        assertThatThrownBy(() -> LeadKeyGenerator.generate("https://example.com", "  "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Source name cannot be null or blank");
    }

    @Test
    void isValid_shouldAcceptValidKey() {
        String validKey = LeadKeyGenerator.generate("https://example.com", "in-memory");
        assertThat(LeadKeyGenerator.isValid(validKey)).isTrue();
    }

    @Test
    void isValid_shouldRejectNull() {
        assertThat(LeadKeyGenerator.isValid(null)).isFalse();
    }

    @Test
    void isValid_shouldRejectBlank() {
        assertThat(LeadKeyGenerator.isValid("  ")).isFalse();
    }

    @Test
    void isValid_shouldRejectInvalidFormat() {
        assertThat(LeadKeyGenerator.isValid("not-a-valid-key")).isFalse();
        assertThat(LeadKeyGenerator.isValid("too-short")).isFalse();
        assertThat(LeadKeyGenerator.isValid("invalid characters!@#$")).isFalse();
    }

    @Test
    void generate_shouldProduceBase64UrlEncodedString() {
        String key = LeadKeyGenerator.generate("https://example.com", "in-memory");

        // SHA-256 = 32 bytes, base64url without padding = 43 chars
        assertThat(key).hasSize(43);
        assertThat(key).matches("[A-Za-z0-9_-]+");
    }
}
