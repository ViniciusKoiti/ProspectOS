package dev.prospectos.integration;

import dev.prospectos.ai.factory.AIProviderFactory;
import dev.prospectos.ai.service.OutreachAIService;
import dev.prospectos.ai.service.ProspectorAIService;
import dev.prospectos.ai.service.ScoringService;
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
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties = {
    "prospectos.ai.enabled=true",
    "prospectos.ai.active-providers=mock",
    "prospectos.scoring.mock.enabled=true",
    "scraper.ai.enabled=false"
})
@ActiveProfiles({"test", "test-pg"})
class AIServicesIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private ProspectorAIService prospectorService;

    @Autowired
    private ScoringService scoringService;

    @Autowired
    private StrategyAIService strategyService;

    @Autowired
    private OutreachAIService outreachService;

    @Autowired
    private AIProviderFactory providerFactory;

    @Autowired
    private CompanyDataService companyDataService;

    @Autowired
    private ICPDataService icpDataService;

    @Test
    void allAIServicesInjectedCorrectly() {
        assertThat(prospectorService).isNotNull();
        assertThat(scoringService).isNotNull();
        assertThat(strategyService).isNotNull();
        assertThat(outreachService).isNotNull();
        assertThat(providerFactory).isNotNull();
        assertThat(companyDataService).isNotNull();
        assertThat(icpDataService).isNotNull();
    }

    @Test
    void servicesHandleInputsGracefully() {
        Company company = createCompanyFromSeed();
        ICP icp = createIcpFromSeed();

        assertThatCode(() -> {
            prospectorService.shouldInvestigateCompany(company, icp);
            prospectorService.enrichCompany(company);
            scoringService.scoreCompany(company, icp);
            strategyService.recommendStrategy(company, icp);
            outreachService.generateOutreach(company, icp);
        }).doesNotThrowAnyException();
    }

    @Test
    void servicesCommunicateWithProvider() {
        Company company = createCompanyFromSeed();
        ICP icp = createIcpFromSeed();

        boolean investigationResult = prospectorService.shouldInvestigateCompany(company, icp);
        assertThat(investigationResult).isNotNull();

        String enrichmentResult = prospectorService.enrichCompany(company);
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
        Company company = createCompanyFromSeed();
        ICP icp = createIcpFromSeed();

        boolean result1 = prospectorService.shouldInvestigateCompany(company, icp);
        boolean result2 = prospectorService.shouldInvestigateCompany(company, icp);

        assertThat(result1).isEqualTo(result2);
    }

    private Company createCompanyFromSeed() {
        CompanyDTO company = companyDataService.findAllCompanies().stream()
            .findFirst()
            .orElse(null);
        assertThat(company)
            .withFailMessage("At least one company should exist in seeded test data.")
            .isNotNull();
        return CompanyMapper.toDomain(company);
    }

    private ICP createIcpFromSeed() {
        ICPDto icp = icpDataService.findAllICPs().stream().findFirst().orElse(null);
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
