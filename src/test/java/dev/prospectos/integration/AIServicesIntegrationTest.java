package dev.prospectos.integration;

import dev.prospectos.ai.client.AIProvider;
import dev.prospectos.ai.factory.AIProviderFactory;
import dev.prospectos.ai.service.OutreachAIService;
import dev.prospectos.ai.service.ProspectorAIService;
import dev.prospectos.ai.service.ScoringAIService;
import dev.prospectos.ai.service.StrategyAIService;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.domain.Website;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "file:.env")
@ActiveProfiles("test")
class AIServicesIntegrationTest {

    @Autowired
    private ProspectorAIService prospectorService;

    @Autowired
    private ScoringAIService scoringService;

    @Autowired
    private StrategyAIService strategyService;

    @Autowired
    private OutreachAIService outreachService;

    @Autowired
    private AIProviderFactory providerFactory;

    private Company createTestCompany() {
        return Company.create("TechCorp", Website.of("https://techcorp.com"), "Software");
    }

    private ICP createTestICP() {
        return ICP.create(
            "SaaS B2B",
            "Growing software companies",
            List.of("Software", "Technology"),
            List.of("Brazil", "United States"),
            List.of("CTO", "VP Engineering"),
            "DevOps and scalability"
        );
    }

    @Test
    void allAIServicesInjectedCorrectly() {
        assertThat(prospectorService).isNotNull();
        assertThat(scoringService).isNotNull();
        assertThat(strategyService).isNotNull();
        assertThat(outreachService).isNotNull();
        assertThat(providerFactory).isNotNull();
    }

    @Test
    void servicesHandleInputsGracefully() {
        Company company = createTestCompany();
        ICP icp = createTestICP();
        
        assertThatCode(() -> {
            prospectorService.shouldInvestigateCompany(company, icp);
            prospectorService.enrichCompanyWithAI(company);
            scoringService.scoreCompany(company, icp);
            strategyService.recommendStrategy(company, icp);
            outreachService.generateOutreach(company, icp);
        }).doesNotThrowAnyException();
    }

    @Test
    void servicesCommunicateWithProvider() {
        Company company = createTestCompany();
        ICP icp = createTestICP();
        
        boolean investigationResult = prospectorService.shouldInvestigateCompany(company, icp);
        assertThat(investigationResult).isNotNull();
        
        String enrichmentResult = prospectorService.enrichCompanyWithAI(company);
        assertThat(enrichmentResult).isNotBlank();
        
        var scoringResult = scoringService.scoreCompany(company, icp);
        assertThat(scoringResult.score()).isBetween(0, 100);
        
        var strategyResult = strategyService.recommendStrategy(company, icp);
        assertThat(strategyResult.channel()).isNotBlank();
        
        var outreachResult = outreachService.generateOutreach(company, icp);
        assertThat(outreachResult.subject()).isNotBlank();
    }

    @Test
    void servicesProvideConsistentResults() {
        Company company = createTestCompany();
        ICP icp = createTestICP();
        
        boolean result1 = prospectorService.shouldInvestigateCompany(company, icp);
        boolean result2 = prospectorService.shouldInvestigateCompany(company, icp);
        
        assertThat(result1).isEqualTo(result2);
    }
}