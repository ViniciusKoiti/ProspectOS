package dev.prospectos.ai.service;

import dev.prospectos.ai.service.AIPromptService.PromptType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for AIPromptService using Spring context.
 * Tests the real Spring lifecycle and prompt loading behavior.
 */
@SpringBootTest(classes = {AIPromptService.class})
@TestPropertySource(properties = {
    "spring.main.banner-mode=off",
    "logging.level.dev.prospectos.ai=DEBUG"
})
class AIPromptServiceIntegrationTest {

    @Autowired
    private AIPromptService promptService;

    @Test
    @DisplayName("Should load B2B prospecting prompt via Spring lifecycle")
    void shouldLoadB2BProspectingPromptViaSpringLifecycle() {
        // Given - Spring already called @PostConstruct during context startup

        // When
        String prompt = promptService.getB2BProspectingPrompt();

        // Then
        assertThat(prompt).isNotBlank();
        assertThat(prompt).contains("B2B prospecting");
        assertThat(prompt).contains("company analysis expert");
        assertThat(prompt).contains("ICP");
    }

    @Test
    @DisplayName("Should load scoring system prompt via Spring lifecycle")
    void shouldLoadScoringPromptViaSpringLifecycle() {
        // Given - Spring lifecycle managed

        // When
        String prompt = promptService.getScoringPrompt();

        // Then
        assertThat(prompt).isNotBlank();
        assertThat(prompt).contains("scoring system");
        assertThat(prompt).contains("0-100");
        assertThat(prompt).contains("ICP fit");
        assertThat(prompt).contains("Interest signals");
    }

    @Test
    @DisplayName("Should get prompts by type consistently")
    void shouldGetPromptsByTypeConsistently() {
        // When
        String b2bPrompt1 = promptService.getPrompt(PromptType.B2B_PROSPECTING);
        String b2bPrompt2 = promptService.getPrompt(PromptType.B2B_PROSPECTING);
        String scoringPrompt = promptService.getPrompt(PromptType.SCORING_SYSTEM);

        // Then
        assertThat(b2bPrompt1).isEqualTo(b2bPrompt2);
        assertThat(b2bPrompt1).isNotEqualTo(scoringPrompt);
        assertThat(b2bPrompt1).isNotBlank();
        assertThat(scoringPrompt).isNotBlank();
    }

    @Test
    @DisplayName("Should report all prompts as loaded after Spring initialization")
    void shouldReportAllPromptsAsLoadedAfterSpringInitialization() {
        // When
        boolean loaded = promptService.arePromptsLoaded();

        // Then
        assertThat(loaded).isTrue();
    }

    @Test
    @DisplayName("Should provide prompt length information")
    void shouldProvidePromptLengthInformation() {
        // When
        Map<PromptType, Integer> lengths = promptService.getPromptLengths();

        // Then
        assertThat(lengths).hasSize(PromptType.values().length);
        assertThat(lengths.get(PromptType.B2B_PROSPECTING)).isPositive();
        assertThat(lengths.get(PromptType.SCORING_SYSTEM)).isPositive();
    }

    @Test
    @DisplayName("Should provide meaningful prompt content")
    void shouldProvideMeaningfulPromptContent() {
        // When
        String b2bPrompt = promptService.getB2BProspectingPrompt();
        String scoringPrompt = promptService.getScoringPrompt();

        // Then
        // B2B prompt should have key sections
        assertThat(b2bPrompt).contains("responsibilities");
        assertThat(b2bPrompt).contains("Principles");
        assertThat(b2bPrompt).contains("Output format");
        assertThat(b2bPrompt).hasSizeGreaterThan(100);

        // Scoring prompt should have scoring criteria
        assertThat(scoringPrompt).contains("30 points"); // ICP fit
        assertThat(scoringPrompt).contains("25 points"); // Interest signals
        assertThat(scoringPrompt).contains("JSON");
        assertThat(scoringPrompt).contains("HOT/WARM/COLD/IGNORE");
        assertThat(scoringPrompt).hasSizeGreaterThan(50);
    }

    @Test
    @DisplayName("Should handle all available prompt types")
    void shouldHandleAllAvailablePromptTypes() {
        // Given
        PromptType[] allTypes = PromptType.values();

        // When & Then
        for (PromptType type : allTypes) {
            assertThatCode(() -> {
                String prompt = promptService.getPrompt(type);
                assertThat(prompt).isNotBlank();
            }).doesNotThrowAnyException();
        }
    }
}
