package dev.prospectos.integration;

import dev.prospectos.ai.client.AIProvider;
import dev.prospectos.ai.factory.AIProviderFactory;
import dev.prospectos.ai.service.OutreachAIService;
import dev.prospectos.ai.service.ProspectorAIService;
import dev.prospectos.ai.service.ScoringAIService;
import dev.prospectos.ai.service.StrategyAIService;
import dev.prospectos.core.api.CompanyDataService;
import dev.prospectos.core.api.ICPDataService;
import dev.prospectos.core.api.dto.CompanyDTO;
import dev.prospectos.core.api.dto.ICPDto;
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
            prospectorService.enrichCompanyWithAI(company);
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
        Company company = createCompanyFromSeed();
        ICP icp = createIcpFromSeed();
        
        boolean result1 = prospectorService.shouldInvestigateCompany(company, icp);
        boolean result2 = prospectorService.shouldInvestigateCompany(company, icp);
        
        assertThat(result1).isEqualTo(result2);
    }

    private Company createCompanyFromSeed() {
        CompanyDTO company = companyDataService.findCompany(1L);
        assertThat(company).isNotNull();
        return Company.create(
            company.name(),
            Website.of(company.website()),
            company.industry()
        );
    }

    private ICP createIcpFromSeed() {
        ICPDto icp = icpDataService.findICP(1L);
        assertThat(icp).isNotNull();
        return ICP.create(
            icp.name(),
            icp.description(),
            icp.targetIndustries(),
            List.of(),
            icp.targetRoles(),
            null
        );
    }
}
