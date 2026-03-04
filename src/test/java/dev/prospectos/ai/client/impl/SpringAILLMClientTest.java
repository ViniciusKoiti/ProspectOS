package dev.prospectos.ai.client.impl;

import dev.prospectos.ai.client.LLMProvider;
import dev.prospectos.ai.dto.OutreachMessage;
import dev.prospectos.ai.dto.PriorityLevel;
import dev.prospectos.ai.dto.ScoringResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpringAILLMClientTest {

    @Test
    void queryAndFunctionsReturnMockContentWhenUnavailable() {
        SpringAILLMClient client = new SpringAILLMClient(null, LLMProvider.GROQ, false);

        assertThat(client.query("hello world")).startsWith("Mock response:");
        assertThat(client.queryWithFunctions("prompt", "scrapeWebsite")).contains("scrapeWebsite");
    }

    @Test
    void queryStructuredReturnsMockObjectsWhenUnavailable() {
        SpringAILLMClient client = new SpringAILLMClient(null, LLMProvider.OPENAI, false);

        ScoringResult scoring = client.queryStructured("prompt", ScoringResult.class);
        OutreachMessage outreach = client.queryStructured("prompt", OutreachMessage.class);

        assertThat(scoring.score()).isEqualTo(85);
        assertThat(scoring.priority()).isEqualTo(PriorityLevel.HOT);
        assertThat(outreach.subject()).contains("Performance optimization");
    }

    @Test
    void exposesProviderAndAvailability() {
        SpringAILLMClient unavailable = new SpringAILLMClient(null, LLMProvider.GROQ, false);
        SpringAILLMClient available = new SpringAILLMClient(null, LLMProvider.OPENAI, true);

        assertThat(unavailable.getProvider()).isEqualTo(LLMProvider.GROQ);
        assertThat(unavailable.isAvailable()).isFalse();
        assertThat(available.getProvider()).isEqualTo(LLMProvider.OPENAI);
        assertThat(available.isAvailable()).isTrue();
    }
}
