package dev.prospectos.infrastructure.service.prospect;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HunterPropertiesTest {

    @Test
    void normalizesDefaultsAndConfiguredLimit() {
        var defaults = new HunterProperties(true, "key", null, null, null);
        var configured = new HunterProperties(true, "key", "https://api.example.com", Duration.ofSeconds(5), 7);

        assertThat(defaults.normalizedBaseUrl()).isEqualTo("https://api.hunter.io/v2/domain-search");
        assertThat(defaults.normalizedTimeout()).isEqualTo(Duration.ofSeconds(15));
        assertThat(defaults.normalizedMaxResults()).isEqualTo(5);
        assertThat(configured.normalizedMaxResults()).isEqualTo(7);
    }
}
