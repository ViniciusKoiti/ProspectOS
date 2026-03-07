package dev.prospectos.ai.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpringAIConfigTest {

    @Test
    void constructorInitializesConfiguration() {
        SpringAIConfig config = new SpringAIConfig();

        assertThat(config).isNotNull();
    }
}
