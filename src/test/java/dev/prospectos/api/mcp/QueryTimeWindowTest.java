package dev.prospectos.api.mcp;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QueryTimeWindowTest {

    @Test
    void parsesSupportedValuesCaseInsensitively() {
        assertThat(QueryTimeWindow.fromValue(" 24H ")).isEqualTo(QueryTimeWindow.TWENTY_FOUR_HOURS);
        assertThat(QueryTimeWindow.fromValue("7d")).isEqualTo(QueryTimeWindow.SEVEN_DAYS);
    }

    @Test
    void rejectsUnsupportedValuesWithClearMessage() {
        assertThatThrownBy(() -> QueryTimeWindow.fromValue("90d"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid timeWindow '90d'. Allowed values: 1h, 24h, 7d, 30d");
    }
}
