package dev.prospectos.ai.service;

import dev.prospectos.ai.exception.AIConfigurationException;
import dev.prospectos.ai.service.AIPromptService.PromptType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AIPromptService.
 * Tests prompt loading, caching, and access functionality.
 */
class AIPromptServiceTest {

    private AIPromptService promptService;

    @BeforeEach
    void setUp() {
        promptService = new AIPromptService();
        // PostConstruct method will be called automatically by Spring in real usage
        // For unit tests, we need to call it manually
        try {
            var loadMethod = AIPromptService.class.getDeclaredMethod("loadPrompts");
            loadMethod.setAccessible(true);
            loadMethod.invoke(promptService);
        } catch (Exception e) {
            fail("Failed to initialize AIPromptService: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should load B2B prospecting prompt successfully")
    void shouldLoadB2BProspectingPrompt() {
        // When
        String prompt = promptService.getB2BProspectingPrompt();

        // Then
        assertNotNull(prompt);
        assertFalse(prompt.trim().isEmpty());
        assertTrue(prompt.contains("B2B prospecting"));
        assertTrue(prompt.contains("company analysis expert"));
        assertTrue(prompt.contains("ICP"));
    }

    @Test
    @DisplayName("Should load scoring system prompt successfully")
    void shouldLoadScoringPrompt() {
        // When
        String prompt = promptService.getScoringPrompt();

        // Then
        assertNotNull(prompt);
        assertFalse(prompt.trim().isEmpty());
        assertTrue(prompt.contains("scoring system"));
        assertTrue(prompt.contains("0-100"));
        assertTrue(prompt.contains("ICP fit"));
        assertTrue(prompt.contains("Interest signals"));
    }

    @Test
    @DisplayName("Should get prompt by type")
    void shouldGetPromptByType() {
        // When
        String b2bPrompt = promptService.getPrompt(PromptType.B2B_PROSPECTING);
        String scoringPrompt = promptService.getPrompt(PromptType.SCORING_SYSTEM);

        // Then
        assertNotNull(b2bPrompt);
        assertNotNull(scoringPrompt);
        assertNotEquals(b2bPrompt, scoringPrompt);
    }

    @Test
    @DisplayName("Should report all prompts as loaded")
    void shouldReportAllPromptsAsLoaded() {
        // When
        boolean loaded = promptService.arePromptsLoaded();

        // Then
        assertTrue(loaded);
    }

    @Test
    @DisplayName("Should provide prompt length information")
    void shouldProvidePromptLengthInformation() {
        // When
        Map<PromptType, Integer> lengths = promptService.getPromptLengths();

        // Then
        assertEquals(PromptType.values().length, lengths.size());
        assertTrue(lengths.get(PromptType.B2B_PROSPECTING) > 0);
        assertTrue(lengths.get(PromptType.SCORING_SYSTEM) > 0);
    }

    @Test
    @DisplayName("Should report lengths consistent with loaded prompt content")
    void shouldReportLengthsConsistentWithLoadedPromptContent() {
        Map<PromptType, Integer> lengths = promptService.getPromptLengths();

        assertEquals(promptService.getB2BProspectingPrompt().length(), lengths.get(PromptType.B2B_PROSPECTING));
        assertEquals(promptService.getScoringPrompt().length(), lengths.get(PromptType.SCORING_SYSTEM));
    }

    @Test
    @DisplayName("Should handle consistent prompt content")
    void shouldHandleConsistentPromptContent() {
        // When - call multiple times
        String prompt1 = promptService.getB2BProspectingPrompt();
        String prompt2 = promptService.getB2BProspectingPrompt();

        // Then
        assertEquals(prompt1, prompt2);
    }

    @Test
    @DisplayName("Should trim whitespace from loaded prompts")
    void shouldTrimWhitespaceFromLoadedPrompts() {
        // When
        String prompt = promptService.getB2BProspectingPrompt();

        // Then
        assertEquals(prompt, prompt.trim());
        assertFalse(prompt.startsWith(" "));
        assertFalse(prompt.endsWith(" "));
    }

    @Test
    @DisplayName("Should provide meaningful prompt content for B2B prospecting")
    void shouldProvideMeaningfulPromptContentForB2B() {
        // When
        String prompt = promptService.getB2BProspectingPrompt();

        // Then
        assertTrue(prompt.contains("responsibilities"));
        assertTrue(prompt.contains("Principles"));
        assertTrue(prompt.contains("Output format"));
        assertTrue(prompt.length() > 100); // Should be substantial content
    }

    @Test
    @DisplayName("Should provide meaningful prompt content for scoring")
    void shouldProvideMeaningfulPromptContentForScoring() {
        // When
        String prompt = promptService.getScoringPrompt();

        // Then
        assertTrue(prompt.contains("30 points")); // ICP fit points
        assertTrue(prompt.contains("25 points")); // Interest signals points
        assertTrue(prompt.contains("JSON"));
        assertTrue(prompt.contains("HOT/WARM/COLD/IGNORE"));
        assertTrue(prompt.length() > 50); // Should be substantial content
    }

    @Test
    @DisplayName("Should handle all available prompt types")
    void shouldHandleAllAvailablePromptTypes() {
        // Given
        PromptType[] allTypes = PromptType.values();

        // When & Then
        for (PromptType type : allTypes) {
            assertDoesNotThrow(() -> {
                String prompt = promptService.getPrompt(type);
                assertNotNull(prompt);
                assertFalse(prompt.trim().isEmpty());
            }, "Should handle prompt type: " + type);
        }
    }

    // Note: Testing error scenarios would require mocking the class loader or file system
    // which is more complex for unit tests. These could be covered in integration tests.
}
