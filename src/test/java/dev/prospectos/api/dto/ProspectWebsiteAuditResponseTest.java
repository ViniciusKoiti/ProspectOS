package dev.prospectos.api.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProspectWebsiteAuditResponseTest {

    @Test
    void exposesAllAuditFields() {
        var response = new ProspectWebsiteAuditResponse(
            82,
            "GOOD",
            true,
            true,
            true,
            false,
            91,
            List.of("Website exposes enough signals for an initial sales review.")
        );

        assertThat(response.score()).isEqualTo(82);
        assertThat(response.status()).isEqualTo("GOOD");
        assertThat(response.secure()).isTrue();
        assertThat(response.scrapeSucceeded()).isTrue();
        assertThat(response.contactInfoDetected()).isTrue();
        assertThat(response.technologySignalsDetected()).isFalse();
        assertThat(response.pageSpeedScore()).isEqualTo(91);
        assertThat(response.findings()).hasSize(1);
    }
}
