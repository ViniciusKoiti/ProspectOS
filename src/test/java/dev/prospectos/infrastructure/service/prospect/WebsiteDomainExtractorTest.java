package dev.prospectos.infrastructure.service.prospect;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WebsiteDomainExtractorTest {

    private final WebsiteDomainExtractor extractor = new WebsiteDomainExtractor();

    @Test
    void extractsHostWithoutWww() {
        assertThat(extractor.extract("https://www.acme.com/path")).isEqualTo("acme.com");
        assertThat(extractor.extract("https://sub.acme.com")).isEqualTo("sub.acme.com");
        assertThat(extractor.extract("bad-url")).isNull();
    }
}
