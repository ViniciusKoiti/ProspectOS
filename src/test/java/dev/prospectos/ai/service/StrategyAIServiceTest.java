package dev.prospectos.ai.service;

import dev.prospectos.ai.client.AIProvider;
import dev.prospectos.ai.dto.StrategyRecommendation;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.domain.Score;
import dev.prospectos.core.domain.Website;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StrategyAIServiceTest {

    @Mock
    private AIProvider aiProvider;

    private StrategyAIService service;

    @BeforeEach
    void setUp() {
        service = new StrategyAIService(aiProvider);
    }

    @Test
    void recommendStrategyBuildsPromptAndReturnsRecommendation() {
        Company company = Company.create("Acme", Website.of("https://acme.com"), "Software");
        company.setAiAnalysis("Promising");
        company.updateScore(Score.of(75), "good");
        ICP icp = ICP.create("Target", "Desc", List.of("Software"), List.of("Brazil"), List.of("CTO"), "Growth");
        StrategyRecommendation recommendation = new StrategyRecommendation(
            "email", "CTO", "immediate", List.of("Scale"), "Value", "Rationale"
        );
        when(aiProvider.generateStrategy(org.mockito.ArgumentMatchers.anyString(), eq(StrategyRecommendation.class)))
            .thenReturn(recommendation);

        StrategyRecommendation result = service.recommendStrategy(company, icp);

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(aiProvider).generateStrategy(promptCaptor.capture(), eq(StrategyRecommendation.class));
        assertThat(result).isSameAs(recommendation);
        assertThat(promptCaptor.getValue())
            .contains("Return JSON with this exact structure")
            .contains("Score: 75")
            .contains("Target Roles: CTO");
    }
}
