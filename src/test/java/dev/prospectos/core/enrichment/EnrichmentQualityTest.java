package dev.prospectos.core.enrichment;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnrichmentQualityTest {

    @Test
    void calculateBuildsQualityMetrics() {
        EnrichmentQuality quality = EnrichmentQuality.calculate(10, 6, 3, 2, 1, 4, 7, 10);

        assertThat(quality.completenessScore()).isEqualTo(0.7);
        assertThat(quality.isHighQuality()).isTrue();
        assertThat(quality.hasValidContacts()).isTrue();
        assertThat(quality.getEmailValidationRate()).isEqualTo(0.6);
    }

    @Test
    void calculateHandlesZeroTotals() {
        EnrichmentQuality quality = EnrichmentQuality.calculate(0, 0, 0, 0, 0, 0, 0, 0);

        assertThat(quality.completenessScore()).isEqualTo(0.0);
        assertThat(quality.isHighQuality()).isFalse();
        assertThat(quality.hasValidContacts()).isFalse();
        assertThat(quality.getEmailValidationRate()).isEqualTo(0.0);
    }
}
