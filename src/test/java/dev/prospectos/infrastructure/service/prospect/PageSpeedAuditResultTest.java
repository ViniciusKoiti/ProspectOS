package dev.prospectos.infrastructure.service.prospect;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PageSpeedAuditResultTest {

    @Test
    void exposesAvailabilityFromScorePresence() {
        var unavailable = PageSpeedAuditResult.unavailable();
        var available = new PageSpeedAuditResult(48, java.util.List.of("Low score"));

        assertThat(unavailable.available()).isFalse();
        assertThat(unavailable.score()).isNull();
        assertThat(available.available()).isTrue();
        assertThat(available.score()).isEqualTo(48);
    }
}
