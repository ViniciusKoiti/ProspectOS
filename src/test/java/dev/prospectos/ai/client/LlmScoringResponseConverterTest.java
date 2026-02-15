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
}
