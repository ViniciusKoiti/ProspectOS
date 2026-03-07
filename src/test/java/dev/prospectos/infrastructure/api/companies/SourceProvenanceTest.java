package dev.prospectos.infrastructure.api.companies;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SourceProvenanceTest {

    @Test
    void factoryMethodBuildsImmutableSnapshot() {
        Instant capturedAt = Instant.now();

        SourceProvenance provenance = SourceProvenance.of(10L, "vector-company", "https://example.com", capturedAt);

        assertThat(provenance.getId()).isNotNull();
        assertThat(provenance.getCompanyExternalId()).isEqualTo(10L);
        assertThat(provenance.getSourceName()).isEqualTo("vector-company");
        assertThat(provenance.getSourceUrl()).isEqualTo("https://example.com");
        assertThat(provenance.getCapturedAt()).isEqualTo(capturedAt);
    }
}
