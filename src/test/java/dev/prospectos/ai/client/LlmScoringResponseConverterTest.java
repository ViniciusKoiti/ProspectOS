package dev.prospectos.ai.client;

import dev.prospectos.ai.dto.PriorityLevel;
import dev.prospectos.ai.dto.ScoringResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LlmScoringResponseConverterTest {

    private final LlmScoringResponseConverter converter =
        new LlmScoringResponseConverter(new LlmStructuredResponseSanitizer());

    @Test
    void convertsJsonWithBreakdownAliasesAndInvalidNewlineInReasoning() {
        String response = """
            {
              "breakdown": {
                "ICP fit": 31,
                "Interest signals": 7,
                "Company size and maturity": 50,
                "Timing and urgency": 9,
                "Contact accessibility": 3
              },
              "priority": "cold",
              "reasoning": "The company does not match all ICP criteria,\r
                           and there are low active signals.",
              "recommendation": "Wait for updated information.",
              "score": 50
            }
            """;

        ScoringResult result = converter.convert(response);

        assertThat(result.score()).isEqualTo(50);
        assertThat(result.priority()).isEqualTo(PriorityLevel.COLD);
        assertThat(result.reasoning()).doesNotContain("\r").doesNotContain("\n");
        assertThat(result.breakdown())
            .containsEntry("icpFit", 30)
            .containsEntry("signals", 7)
            .containsEntry("companySize", 20)
            .containsEntry("timing", 9)
            .containsEntry("accessibility", 3);
    }

    @Test
    void fallsBackSafelyWhenNoJsonIsPresent() {
        String response = "Unable to provide structured output right now.";

        ScoringResult result = converter.convert(response);

        assertThat(result.score()).isEqualTo(0);
        assertThat(result.priority()).isEqualTo(PriorityLevel.COLD);
        assertThat(result.breakdown().get("icpFit")).isEqualTo(0);
        assertThat(result.reasoning()).isNotBlank();
    }

    @Test
    void fallsBackToRegexExtractionWhenPayloadIsNotJson() {
        String response = """
            scoring output: "score": 73, "priority": "hot",
            "reasoning": "Strong ICP fit and recent buying signal.",
            "recommendation": "Start outbound immediately."
            """;

        ScoringResult result = converter.convert(response);

        assertThat(result.score()).isEqualTo(73);
        assertThat(result.priority()).isEqualTo(PriorityLevel.HOT);
        assertThat(result.reasoning()).isEqualTo("Strong ICP fit and recent buying signal.");
        assertThat(result.recommendation()).isEqualTo("Start outbound immediately.");
    }

    @Test
    void defaultsPriorityWhenJsonContainsUnknownPriority() {
        String response = """
            {
              "score": 42,
              "priority": "urgent",
              "reasoning": "Test reasoning",
              "recommendation": "Test recommendation"
            }
            """;

        ScoringResult result = converter.convert(response);

        assertThat(result.score()).isEqualTo(42);
        assertThat(result.priority()).isEqualTo(PriorityLevel.COLD);
    }
}
