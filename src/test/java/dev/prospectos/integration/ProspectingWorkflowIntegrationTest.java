package dev.prospectos.integration;

import dev.prospectos.ai.dto.OutreachMessage;
import dev.prospectos.ai.dto.ScoringResult;
import dev.prospectos.ai.dto.StrategyRecommendation;
import dev.prospectos.ai.service.OutreachAIService;
import dev.prospectos.ai.service.ProspectorAIService;
import dev.prospectos.ai.service.ScoringAIService;
import dev.prospectos.ai.service.StrategyAIService;
import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.mapper.CompanyMapper;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.support.PostgresIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test", "test-pg"})
class ProspectingWorkflowIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private ProspectorAIService prospectorService;

    @Autowired
    private ScoringAIService scoringService;

    @Autowired
    private StrategyAIService strategyService;

    @Autowired
    private OutreachAIService outreachService;

    @Autowired
    private CompanyDataService companyDataService;

    @Autowired
    private ICPDataService icpDataService;

    @Autowired
    private Environment environment;

    @org.junit.jupiter.api.BeforeEach
    void logActiveProfiles() {
        System.out.println("Active profiles: " + String.join(",", environment.getActiveProfiles()));
    }

    @Test
    void completeWorkflowHighPotential() {
        Company company = createCompanyFromSeed();
        ICP icp = createIcpFromSeed();

        boolean shouldInvestigate = prospectorService.shouldInvestigateCompany(company, icp);

        if (shouldInvestigate) {
            String aiAnalysis = prospectorService.enrichCompany(company);
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
        Company company = createCompanyFromSeed();
        ICP icp = createIcpFromSeed();

        boolean shouldInvestigate = prospectorService.shouldInvestigateCompany(company, icp);

        if (!shouldInvestigate) {
            // Low potential correctly identified
        } else {
            // Continue with workflow to test all components
            String aiAnalysis = prospectorService.enrichCompany(company);
            assertThat(aiAnalysis).isNotBlank();

            ScoringResult score = scoringService.scoreCompany(company, icp);
            assertThat(score.score()).isBetween(0, 100);
        }
    }

    @Test
    void workflowScalability() {
        List<Company> companies = companyDataService.findAllCompanies()
            .stream()
            .map(CompanyMapper::toDomain)
            .toList();
        ICP icp = createIcpFromSeed();

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
        Company minimalCompany = createCompanyFromSeed();
        ICP icp = createIcpFromSeed();

        assertThatCode(() -> {
            boolean shouldInvestigate = prospectorService.shouldInvestigateCompany(minimalCompany, icp);

            if (shouldInvestigate) {
                String aiAnalysis = prospectorService.enrichCompany(minimalCompany);
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

    private Company createCompanyFromSeed() {
        CompanyDTO company = companyDataService.findAllCompanies().stream()
            .findFirst()
            .orElse(null);
        assertThat(company).isNotNull();
        return CompanyMapper.toDomain(company);
    }

    private ICP createIcpFromSeed() {
        ICPDto icp = icpDataService.findAllICPs().stream()
            .findFirst()
            .orElse(null);
        assertThat(icp).isNotNull();
        return ICP.create(
            icp.name() != null ? icp.name() : "Test ICP",
            icp.description(),
            List.of("Technology"),
            List.of(),
            List.of("CTO"),
            null
        );
    }
}
