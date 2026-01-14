package dev.prospectos.integration;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.request.CompanyCreateRequest;
import dev.prospectos.infrastructure.service.inmemory.InMemoryCoreDataStore;
import dev.prospectos.infrastructure.service.scoring.CompanyScoringService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration test for scoring flow using in-memory persistence.
 */
@SpringBootTest
@ActiveProfiles("test")
class ScoringPersistenceIntegrationTest {

    @Autowired
    private CompanyScoringService companyScoringService;

    @Autowired
    private CompanyDataService companyDataService;

    @Autowired
    private ICPDataService icpDataService;

    @Autowired
    private InMemoryCoreDataStore dataStore;

    @Test
    void scoringFlow_PersistsScoreForCreatedCompany() {
        CompanyCreateRequest companyRequest = new CompanyCreateRequest(
            "Test Scoring Company",
            "Technology",
            "https://testscoringcompany.com",
            "A company created specifically for testing scoring flow",
            null,
            null,
            "SMALL"
        );

        CompanyDTO createdCompany = companyDataService.createCompany(companyRequest);
        ICPDto testIcp = icpDataService.findICP(1L);

        ScoreDTO score = companyScoringService.scoreCompany(createdCompany.id(), testIcp.id());

        assertThat(score.value()).isBetween(0, 100);
        assertThat(score.category()).isNotBlank();
        assertThat(score.reasoning()).isNotBlank();
        assertThat(dataStore.companyScores()).containsKey(createdCompany.id());
        assertThat(dataStore.companyScores().get(createdCompany.id()).value()).isEqualTo(score.value());
    }

    @Test
    void scoringFlow_RejectsMissingCompany() {
        ICPDto testIcp = icpDataService.findICP(1L);

        assertThatThrownBy(() -> companyScoringService.scoreCompany(999L, testIcp.id()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Company not found");
    }
}
