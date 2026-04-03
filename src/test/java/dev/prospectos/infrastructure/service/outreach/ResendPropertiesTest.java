package dev.prospectos.infrastructure.service.outreach;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResendPropertiesTest {

    @Test
    void normalizesDefaults() {
        var defaults = new ResendProperties(true, "key", null, null);

        assertThat(defaults.normalizedBaseUrl()).isEqualTo("https://api.resend.com/emails");
        assertThat(defaults.normalizedTimeout()).isEqualTo(Duration.ofSeconds(15));
    }
}
