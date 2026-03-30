package dev.prospectos.api.mcp;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoutingStrategyTest {

    @Test
    void parsesSupportedStrategiesCaseInsensitively() {
        assertThat(RoutingStrategy.fromValue(" cost_optimized ")).isEqualTo(RoutingStrategy.COST_OPTIMIZED);
        assertThat(RoutingStrategy.fromValue("BALANCED")).isEqualTo(RoutingStrategy.BALANCED);
    }

    @Test
    void rejectsUnsupportedStrategiesWithClearMessage() {
        assertThatThrownBy(() -> RoutingStrategy.fromValue("FASTEST"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid routing strategy 'FASTEST'. Allowed values: COST_OPTIMIZED, PERFORMANCE_OPTIMIZED, BALANCED");
    }
}