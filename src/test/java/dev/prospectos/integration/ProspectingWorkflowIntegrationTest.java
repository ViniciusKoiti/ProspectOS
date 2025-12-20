package dev.prospectos.integration;

import dev.prospectos.ai.dto.OutreachMessage;
import dev.prospectos.ai.dto.ScoringResult;
import dev.prospectos.ai.dto.StrategyRecommendation;
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
class ProspectingWorkflowIntegrationTest {

    @Autowired
    private ProspectorAIService prospectorService;

    @Autowired
    private ScoringAIService scoringService;

    @Autowired
    private StrategyAIService strategyService;

    @Autowired
    private OutreachAIService outreachService;

    private Company createHighPotentialCompany() {
        return Company.create(
            "CloudTech Solutions",
            Website.of("https://cloudtech.com"),
            "Software"
        );
    }

    private Company createLowPotentialCompany() {
        return Company.create(
            "Local Restaurant",
            Website.of("https://localrestaurant.com"),
            "Food & Beverage"
        );
    }

    private ICP createTechFocusedICP() {
        return ICP.create(
            "Enterprise SaaS",
            "Fast-growing technology companies",
            List.of("Software", "Technology", "SaaS"),
            List.of("United States", "Brazil", "Canada"),
            List.of("CTO", "VP Engineering", "Head of DevOps"),
            "Cloud infrastructure and scalability"
        );
    }

    @Test
    void completeWorkflowHighPotential() {
        Company company = createHighPotentialCompany();
        ICP icp = createTechFocusedICP();
        
        boolean shouldInvestigate = prospectorService.shouldInvestigateCompany(company, icp);
        
        if (shouldInvestigate) {
            String aiAnalysis = prospectorService.enrichCompanyWithAI(company);
            assertThat(aiAnalysis).isNotBlank();
            assertThat(aiAnalysis.length()).isGreaterThan(50);
            
            ScoringResult score = scoringService.scoreCompany(company, icp);
            assertThat(score.score()).isBetween(0, 100);
            assertThat(score.priority()).isNotNull();
            assertThat(score.reasoning()).isNotBlank();
            assertThat(score.breakdown()).isNotEmpty();
            assertThat(score.recommendation()).isNotBlank();
            
            StrategyRecommendation strategy = strategyService.recommendStrategy(company, icp);
            assertThat(strategy.channel()).isNotBlank();
            assertThat(strategy.targetRole()).isNotBlank();
            assertThat(strategy.timing()).isNotBlank();
            assertThat(strategy.painPoints()).isNotEmpty();
            assertThat(strategy.valueProposition()).isNotBlank();
            assertThat(strategy.approachRationale()).isNotBlank();
            
            OutreachMessage outreach = outreachService.generateOutreach(company, icp);
            assertThat(outreach.subject()).isNotBlank();
            assertThat(outreach.body()).isNotBlank();
            assertThat(outreach.channel()).isNotBlank();
            assertThat(outreach.tone()).isNotBlank();
            assertThat(outreach.callsToAction()).isNotEmpty();
            
            assertThat(strategy.channel()).isEqualToIgnoringCase(outreach.channel());
        }
    }

    @Test
    void completeWorkflowLowPotential() {
        Company company = createLowPotentialCompany();
        ICP icp = createTechFocusedICP();
        
        boolean shouldInvestigate = prospectorService.shouldInvestigateCompany(company, icp);
        
        if (!shouldInvestigate) {
            // Low potential correctly identified
        } else {
            // Continue with workflow to test all components
            String aiAnalysis = prospectorService.enrichCompanyWithAI(company);
            assertThat(aiAnalysis).isNotBlank();
            
            ScoringResult score = scoringService.scoreCompany(company, icp);
            assertThat(score.score()).isLessThan(50);
        }
    }

    @Test
    void workflowScalability() {
        List<Company> companies = List.of(
            Company.create("TechStart1", Website.of("https://techstart1.com"), "Software"),
            Company.create("TechStart2", Website.of("https://techstart2.com"), "Software"),
            Company.create("TechStart3", Website.of("https://techstart3.com"), "Software")
        );
        
        ICP icp = createTechFocusedICP();
        
        for (Company company : companies) {
            boolean shouldInvestigate = prospectorService.shouldInvestigateCompany(company, icp);
            
            if (shouldInvestigate) {
                ScoringResult score = scoringService.scoreCompany(company, icp);
                assertThat(score.score()).isBetween(0, 100);
                
                StrategyRecommendation strategy = strategyService.recommendStrategy(company, icp);
                assertThat(strategy.channel()).isNotBlank();
            }
        }
    }

    @Test
    void workflowHandlesEdgeCases() {
        Company minimalCompany = Company.create(
            "MinimalCorp",
            Website.of("https://minimal.com"),
            "Unknown"
        );
        
        ICP icp = createTechFocusedICP();
        
        assertThatCode(() -> {
            boolean shouldInvestigate = prospectorService.shouldInvestigateCompany(minimalCompany, icp);
            
            if (shouldInvestigate) {
                String aiAnalysis = prospectorService.enrichCompanyWithAI(minimalCompany);
                assertThat(aiAnalysis).isNotBlank();
                
                ScoringResult score = scoringService.scoreCompany(minimalCompany, icp);
                assertThat(score.score()).isBetween(0, 100);
                
                StrategyRecommendation strategy = strategyService.recommendStrategy(minimalCompany, icp);
                assertThat(strategy.channel()).isNotBlank();
                
                OutreachMessage outreach = outreachService.generateOutreach(minimalCompany, icp);
                assertThat(outreach.subject()).isNotBlank();
            }
        }).doesNotThrowAnyException();
    }
}