package dev.prospectos.ai.service;

import dev.prospectos.ai.client.AIProvider;
import dev.prospectos.ai.client.LLMClient;
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
class ProspectorAIServiceTest {

    @Mock
    private AIProvider aiProvider;

    @Mock
    private LLMClient llmClient;

    private ProspectorAIService service;

    @BeforeEach
    void setUp() {
        service = new ProspectorAIService(aiProvider);
    }

    @Test
    void shouldInvestigateCompanyBuildsPromptAndReturnsProviderDecision() {
        Company company = company();
        ICP icp = icp();
        when(aiProvider.analyzeICPFit(org.mockito.ArgumentMatchers.anyString())).thenReturn(true);

        boolean result = service.shouldInvestigateCompany(company, icp);

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(aiProvider).analyzeICPFit(promptCaptor.capture());
        assertThat(result).isTrue();
        assertThat(promptCaptor.getValue())
            .contains("Company: Acme")
            .contains("Website: https://acme.com")
            .contains("Target Industries: Software, Technology")
            .contains("Interest Theme: Growth");
    }

    @Test
    void enrichCompanyDelegatesToProvider() {
        Company company = company();
        when(aiProvider.enrichCompanyData(org.mockito.ArgumentMatchers.anyString())).thenReturn("analysis");

        String result = service.enrichCompany(company);

        assertThat(result).isEqualTo("analysis");
        verify(aiProvider).enrichCompanyData(org.mockito.ArgumentMatchers.contains("Maximum 200 words"));
    }

    @Test
    void recommendApproachStrategyUsesProviderClientFunctionCall() {
        Company company = company();
        company.setAiAnalysis("Strong fit");
        company.updateScore(Score.of(88), "great");
        ICP icp = icp();
        when(aiProvider.getClient()).thenReturn(llmClient);
        when(llmClient.queryWithFunctions(org.mockito.ArgumentMatchers.anyString(), eq("analyzeCompanySignals")))
            .thenReturn("Use LinkedIn");

        String result = service.recommendApproachStrategy(company, icp);

        assertThat(result).isEqualTo("Use LinkedIn");
        verify(llmClient).queryWithFunctions(org.mockito.ArgumentMatchers.contains("Best outreach channel"), eq("analyzeCompanySignals"));
    }

    private Company company() {
        return Company.create("Acme", Website.of("https://acme.com"), "Software");
    }

    private ICP icp() {
        return ICP.create("Target", "Desc", List.of("Software", "Technology"), List.of("Brazil"), List.of("CTO"), "Growth");
    }
}
