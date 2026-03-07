package dev.prospectos.ai.client.mock;

import dev.prospectos.ai.dto.OutreachMessage;
import dev.prospectos.ai.dto.PriorityLevel;
import dev.prospectos.ai.dto.ScoringResult;
import dev.prospectos.ai.dto.StrategyRecommendation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockResponseFactoryTest {

    @Test
    void createsKnownStructuredMocks() {
        ScoringResult scoring = MockResponseFactory.createMockResponse(ScoringResult.class, "Groq");
        StrategyRecommendation strategy = MockResponseFactory.createMockResponse(StrategyRecommendation.class, "Groq");
        OutreachMessage outreach = MockResponseFactory.createMockResponse(OutreachMessage.class, "Groq");

        assertThat(scoring.score()).isEqualTo(85);
        assertThat(scoring.priority()).isEqualTo(PriorityLevel.HOT);
        assertThat(strategy.channel()).isEqualTo("email");
        assertThat(outreach.subject()).contains("Performance optimization");
    }

    @Test
    void returnsNullForUnknownType() {
        assertThat(MockResponseFactory.createMockResponse(String.class, "Groq")).isNull();
    }
}
