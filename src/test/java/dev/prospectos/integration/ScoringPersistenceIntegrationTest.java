package dev.prospectos.integration;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.request.CompanyCreateRequest;
import dev.prospectos.support.PostgresIntegrationTestBase;
import dev.prospectos.infrastructure.service.scoring.CompanyScoringService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration test for scoring flow and persisted score updates.
 */
@SpringBootTest
@ActiveProfiles({"test", "test-pg"})
class ScoringPersistenceIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private CompanyScoringService companyScoringService;

    @Autowired
    private CompanyDataService companyDataService;

    @Autowired
    private ICPDataService icpDataService;

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
        ICPDto testIcp = icpDataService.findAllICPs().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Expected seeded ICP data for tests"));

        ScoreDTO score = companyScoringService.scoreCompany(createdCompany.id(), testIcp.id());

        assertThat(score.value()).isBetween(0, 100);
        assertThat(score.category()).isNotBlank();
        assertThat(score.reasoning()).isNotBlank();

        CompanyDTO updatedCompany = companyDataService.findCompany(createdCompany.id());
        assertThat(updatedCompany).isNotNull();
        assertThat(updatedCompany.score()).isNotNull();
        assertThat(updatedCompany.score().value()).isEqualTo(score.value());
    }

    @Test
    void scoringFlow_RejectsMissingCompany() {
        ICPDto testIcp = icpDataService.findAllICPs().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Expected seeded ICP data for tests"));

        assertThatThrownBy(() -> companyScoringService.scoreCompany(999L, testIcp.id()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Company not found");
    }
}
