package dev.prospectos.core.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WebsiteTest {

    @Test
    void ofNormalizesUrlExtractsDomainAndSupportsEquality() {
        Website website = Website.of("www.Example.com");
        Website same = Website.of("https://www.Example.com");

        assertThat(website.getUrl()).isEqualTo("https://www.Example.com");
        assertThat(website.getDomain()).isEqualTo("example.com");
        assertThat(website.isSecure()).isTrue();
        assertThat(website).isEqualTo(same);
        assertThat(website.hashCode()).isEqualTo(same.hashCode());
        assertThat(website.toString()).isEqualTo("https://www.Example.com");
    }

    @Test
    void ofRejectsInvalidValues() {
        assertThatThrownBy(() -> Website.of(" "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Website URL cannot be null or empty");

        assertThatThrownBy(() -> Website.of("http://"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid website URL");
    }

    @Test
    void ofKeepsHttpSchemeForExistingNonSecureUrls() {
        Website website = Website.of("  http://WWW.Example.com/path  ");

        assertThat(website.getUrl()).isEqualTo("http://WWW.Example.com/path");
        assertThat(website.getDomain()).isEqualTo("example.com");
        assertThat(website.isSecure()).isFalse();
    }
}
