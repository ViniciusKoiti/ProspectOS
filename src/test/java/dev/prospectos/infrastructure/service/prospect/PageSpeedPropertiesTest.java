package dev.prospectos.infrastructure.service.prospect;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PageSpeedPropertiesTest {

    @Test
    void normalizesDefaultsAndSupportedStrategy() {
        var defaults = new PageSpeedProperties(true, "key", null, null, null, null);
        var desktop = new PageSpeedProperties(true, "key", "https://example.com", "desktop", "en-US", Duration.ofSeconds(5));

        assertThat(defaults.normalizedBaseUrl()).isEqualTo("https://www.googleapis.com/pagespeedonline/v5/runPagespeed");
        assertThat(defaults.normalizedStrategy()).isEqualTo("mobile");
        assertThat(defaults.normalizedLocale()).isEqualTo("pt-BR");
        assertThat(defaults.normalizedTimeout()).isEqualTo(Duration.ofSeconds(30));
        assertThat(desktop.normalizedStrategy()).isEqualTo("desktop");
        assertThat(desktop.normalizedLocale()).isEqualTo("en-US");
    }
}
