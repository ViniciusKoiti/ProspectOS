package dev.prospectos.ai.service;

import dev.prospectos.ai.client.AIProvider;
import dev.prospectos.ai.dto.PriorityLevel;
import dev.prospectos.ai.dto.ScoringResult;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.domain.Website;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScoringAIServiceTest {

    @Mock
    private AIProvider aiProvider;

    private ScoringAIService service;

    @BeforeEach
    void setUp() {
        service = new ScoringAIService(aiProvider);
    }

    @Test
    void scoreCompanyBuildsPromptAndReturnsStructuredResult() {
        Company company = Company.create("Acme", Website.of("https://acme.com"), "Software");
        company.setAiAnalysis("Growing fast");
        ICP icp = ICP.create("Target", "Desc", List.of("Software"), List.of("Brazil"), List.of("CTO"), "Growth");
        ScoringResult expected = new ScoringResult(90, PriorityLevel.HOT, "Strong fit", Map.of("icpFit", 30), "Reach out");
        when(aiProvider.calculateScore(org.mockito.ArgumentMatchers.anyString(), eq(ScoringResult.class))).thenReturn(expected);

        ScoringResult result = service.scoreCompany(company, icp);

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(aiProvider).calculateScore(promptCaptor.capture(), eq(ScoringResult.class));
        assertThat(result).isSameAs(expected);
        assertThat(promptCaptor.getValue())
            .contains("Calculate the score (0-100)")
            .contains("Use priority as one of: HOT, WARM, COLD, IGNORE")
            .contains("Active Signals: false");
    }
}
