package dev.prospectos.core.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PriorityTest {

    @Test
    void exposesDescriptionsForAllLevels() {
        assertThat(Priority.HOT.getDescription()).isEqualTo("Hot");
        assertThat(Priority.WARM.getDescription()).isEqualTo("Warm");
        assertThat(Priority.COLD.getDescription()).isEqualTo("Cold");
        assertThat(Priority.IGNORE.getDescription()).isEqualTo("Ignore");
    }
}
