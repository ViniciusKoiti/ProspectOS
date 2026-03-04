package dev.prospectos.ai.service;

import dev.prospectos.ai.client.AIProvider;
import dev.prospectos.ai.dto.OutreachMessage;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutreachAIServiceTest {

    @Mock
    private AIProvider aiProvider;

    private OutreachAIService service;

    @BeforeEach
    void setUp() {
        service = new OutreachAIService(aiProvider);
    }

    @Test
    void generateOutreachBuildsPromptAndReturnsMessage() {
        Company company = Company.create("Acme", Website.of("https://acme.com"), "Software");
        company.setAiAnalysis("Research");
        company.setRecommendedApproach("Email CTO");
        ICP icp = ICP.create("Target", "Desc", List.of("Software"), List.of("Brazil"), List.of("CTO"), "Growth");
        OutreachMessage message = new OutreachMessage("Subject", "Body", "email", "consultative", new String[]{"Reply"});
        when(aiProvider.generateOutreach(org.mockito.ArgumentMatchers.anyString(), eq(OutreachMessage.class))).thenReturn(message);

        OutreachMessage result = service.generateOutreach(company, icp);

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(aiProvider).generateOutreach(promptCaptor.capture(), eq(OutreachMessage.class));
        assertThat(result).isSameAs(message);
        assertThat(promptCaptor.getValue())
            .contains("Create a highly personalized B2B outreach message")
            .contains("Recommended Strategy: Email CTO")
            .contains("Return JSON");
    }
}
