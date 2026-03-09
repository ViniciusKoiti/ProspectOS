package dev.prospectos.ai.service;

import dev.prospectos.ai.service.AIPromptService.PromptType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Simplified tests for AIPromptService using minimal Spring configuration.
 * Avoids complex dependency injection issues.
 */
@SpringJUnitConfig(AIPromptServiceSimpleTest.TestConfig.class)
class AIPromptServiceSimpleTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AIPromptService aiPromptService() {
            return new AIPromptService();
        }
    }

    @Autowired
    private AIPromptService promptService;

    @Test
    @DisplayName("Should create AIPromptService successfully")
    void shouldCreateAIPromptServiceSuccessfully() {
        assertThat(promptService).isNotNull();
        assertThat(promptService.arePromptsLoaded()).isTrue();
    }

    @Test
    @DisplayName("Should load and provide B2B prospecting prompt")
    void shouldLoadAndProvideB2BProspectingPrompt() {
        String prompt = promptService.getB2BProspectingPrompt();

        assertThat(prompt).isNotBlank();
        assertThat(prompt).contains("B2B prospecting");
        assertThat(prompt).contains("ICP");
    }

    @Test
    @DisplayName("Should load and provide scoring prompt")
    void shouldLoadAndProvideScorePrompt() {
        String prompt = promptService.getScoringPrompt();

        assertThat(prompt).isNotBlank();
        assertThat(prompt).contains("scoring system");
        assertThat(prompt).contains("0-100");
    }

    @Test
    @DisplayName("Should provide consistent prompt access")
    void shouldProvideConsistentPromptAccess() {
        String prompt1 = promptService.getB2BProspectingPrompt();
        String prompt2 = promptService.getB2BProspectingPrompt();

        assertThat(prompt1).isEqualTo(prompt2);
    }

    @Test
    @DisplayName("Should provide prompt length information")
    void shouldProvidePromptLengthInformation() {
        Map<PromptType, Integer> lengths = promptService.getPromptLengths();

        assertThat(lengths).hasSize(PromptType.values().length);
        assertThat(lengths.get(PromptType.B2B_PROSPECTING)).isPositive();
        assertThat(lengths.get(PromptType.SCORING_SYSTEM)).isPositive();
    }
}
