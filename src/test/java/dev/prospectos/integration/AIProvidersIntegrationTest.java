package dev.prospectos.integration;

import dev.prospectos.ai.client.AIProvider;
import dev.prospectos.ai.client.LLMProvider;
import dev.prospectos.ai.dto.ScoringResult;
import dev.prospectos.ai.dto.StrategyRecommendation;
import dev.prospectos.ai.dto.OutreachMessage;
import dev.prospectos.ai.factory.AIProviderFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "file:.env")
@ActiveProfiles("test")
class AIProvidersIntegrationTest {

    @Autowired
    private AIProviderFactory providerFactory;

    @Test
    void factoryCreatesPrimaryProvider() {
        AIProvider provider = providerFactory.createPrimaryProvider();

        assertThat(provider).isNotNull();
        assertThat(provider.getClient()).isNotNull();
        assertThat(provider.getClient().getProvider()).isNotNull();
    }

    @Test
    void factoryCreatesAllProviders() {
        for (LLMProvider providerType : LLMProvider.values()) {
            AIProvider provider = providerFactory.createProvider(providerType);

            assertThat(provider).isNotNull();
            assertThat(provider.getClient()).isNotNull();
            assertThat(provider.getClient().getProvider()).isEqualTo(providerType);
        }
    }

    @Test
    void providerHandlesICPFitAnalysis() {
        AIProvider provider = providerFactory.createPrimaryProvider();
        String prompt = "Company: TechCorp, Industry: Software. ICP: DevOps. Fit?";

        boolean result = provider.analyzeICPFit(prompt);

        assertThat(result).isNotNull();
    }

    @Test
    void providerGeneratesScoringResults() {
        AIProvider provider = providerFactory.createPrimaryProvider();
        String prompt = "Score company TechCorp from Software industry for ICP DevOps";

        ScoringResult result = provider.calculateScore(prompt, ScoringResult.class);

        assertThat(result).isNotNull();
        assertThat(result.score()).isBetween(0, 100);
        assertThat(result.priority()).isNotNull();
        assertThat(result.reasoning()).isNotBlank();
    }

    @Test
    void providerGeneratesStrategyRecommendations() {
        AIProvider provider = providerFactory.createPrimaryProvider();
        String prompt = "Strategy for TechCorp from Software industry";

        StrategyRecommendation result = provider.generateStrategy(prompt, StrategyRecommendation.class);

        assertThat(result).isNotNull();
        assertThat(result.channel()).isIn("email", "linkedin", "phone", "event");
        assertThat(result.targetRole()).isNotBlank();
        assertThat(result.timing()).isIn("immediate", "this_week", "this_month", "wait");
        assertThat(result.painPoints()).isNotEmpty();
        assertThat(result.valueProposition()).isNotBlank();
        assertThat(result.approachRationale()).isNotBlank();
    }

    @Test
    void providerGeneratesOutreachMessages() {
        AIProvider provider = providerFactory.createPrimaryProvider();
        String prompt = "Outreach for TechCorp about DevOps";

        OutreachMessage result = provider.generateOutreach(prompt, OutreachMessage.class);

        assertThat(result).isNotNull();
        assertThat(result.subject()).isNotBlank();
        assertThat(result.body()).isNotBlank();
        assertThat(result.channel()).isIn("email", "linkedin", "phone");
        assertThat(result.tone()).isIn("formal", "casual", "consultative");
        assertThat(result.callsToAction()).isNotEmpty();
    }

    @Test
    void providerHandlesErrorsGracefully() {
        AIProvider provider = providerFactory.createPrimaryProvider();

        assertThatCode(() -> {
            provider.analyzeICPFit("");
            provider.analyzeICPFit(null);
        }).doesNotThrowAnyException();
    }

    @Test
    void mockProviderIsAlwaysAvailable() {
        AIProvider mockProvider = providerFactory.createProvider(LLMProvider.MOCK);
        assertThat(mockProvider.isAvailable()).isTrue();
    }
}
