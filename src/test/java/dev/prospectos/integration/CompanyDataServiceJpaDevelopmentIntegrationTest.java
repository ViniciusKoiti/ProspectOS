package dev.prospectos.integration;

import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.request.CompanyCreateRequest;
import dev.prospectos.api.dto.request.CompanyUpdateRequest;
import dev.prospectos.api.dto.request.ICPCreateRequest;
import dev.prospectos.api.ICPDataService;
import dev.prospectos.core.repository.CompanyDomainRepository;
import dev.prospectos.support.PostgresIntegrationTestBase;
import dev.prospectos.infrastructure.adapter.CompanyRepositoryAdapter;
import dev.prospectos.infrastructure.adapter.ICPRepositoryAdapter;
import dev.prospectos.infrastructure.service.jpa.CompanyDataServiceJpa;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles({"development", "test-pg"})
@Import({
    CompanyRepositoryAdapter.class,
    ICPRepositoryAdapter.class,
    CompanyDataServiceJpa.class,
    dev.prospectos.infrastructure.service.jpa.ICPDataServiceJpa.class
})
class CompanyDataServiceJpaDevelopmentIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private CompanyDataServiceJpa companyDataService;

    @Autowired
    private ICPDataService icpDataService;

    @Autowired
    private CompanyDomainRepository companyRepository;

    @Test
    void createFindUpdateAndDeleteCompanyPersistThroughJpa() {
        CompanyDTO created = companyDataService.createCompany(new CompanyCreateRequest(
            "Acme",
            "Technology",
            "https://acme.com",
            "Initial description",
            "BR",
            "Sao Paulo",
            "SMALL"
        ));

        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("Acme");
        assertThat(created.location()).isEqualTo("Sao Paulo, BR");

        CompanyDTO found = companyDataService.findCompany(created.id());

        assertThat(found).isNotNull();
        assertThat(found.website()).isEqualTo("https://acme.com");
        assertThat(found.industry()).isEqualTo("Technology");

        CompanyDTO updated = companyDataService.updateCompany(created.id(), new CompanyUpdateRequest(
            "Acme Updated",
            "SaaS",
            "https://acme.com",
            "Updated description",
            "US",
            "Austin",
            "MEDIUM"
        ));

        assertThat(updated).isNotNull();
        assertThat(updated.name()).isEqualTo("Acme Updated");
        assertThat(updated.industry()).isEqualTo("SaaS");
        assertThat(updated.description()).isEqualTo("Updated description");
        assertThat(updated.location()).isEqualTo("Austin, US");

        assertThat(companyDataService.deleteCompany(created.id())).isTrue();
        assertThat(companyDataService.findCompany(created.id())).isNull();
    }

    @Test
    void findsCompanyByWebsiteAndPersistsUpdatedScore() {
        CompanyDTO created = companyDataService.createCompany(new CompanyCreateRequest(
            "Platform Co",
            "Technology",
            "https://platform.example.com",
            null,
            null,
            null,
            "STARTUP"
        ));

        CompanyDTO byWebsite = companyDataService.findByWebsite("platform.example.com");

        assertThat(byWebsite).isNotNull();
        assertThat(byWebsite.id()).isEqualTo(created.id());

        companyDataService.updateCompanyScore(created.id(), new ScoreDTO(91, "HOT", "High fit"));

        CompanyDTO rescored = companyDataService.findCompany(created.id());

        assertThat(rescored.score()).isNotNull();
        assertThat(rescored.score().value()).isEqualTo(91);
        assertThat(rescored.score().category()).isEqualTo("HOT");
    }

    @Test
    void findsCompaniesByIcpUsingPersistedIndustries() {
        ICPCreateRequest icpRequest = new ICPCreateRequest(
            "Tech ICP",
            "Targets technology and finance",
            List.of("Technology", "Finance"),
            List.of("BR"),
            List.of("CTO"),
            "Modernization",
            null,
            null,
            null
        );
        Long icpId = icpDataService.createICP(icpRequest).id();

        companyDataService.createCompany(new CompanyCreateRequest(
            "Tech Co",
            "Technology",
            "https://tech.example.com",
            null,
            null,
            null,
            "SMALL"
        ));
        companyDataService.createCompany(new CompanyCreateRequest(
            "Finance Co",
            "Finance",
            "https://finance.example.com",
            null,
            null,
            null,
            "MEDIUM"
        ));
        companyDataService.createCompany(new CompanyCreateRequest(
            "Retail Co",
            "Retail",
            "https://retail.example.com",
            null,
            null,
            null,
            "MEDIUM"
        ));

        List<CompanyDTO> companies = companyDataService.findCompaniesByICP(icpId);

        assertThat(companies)
            .extracting(CompanyDTO::name)
            .containsExactlyInAnyOrder("Tech Co", "Finance Co");
    }

    @Test
    void rejectsInvalidCompanySize() {
        assertThatThrownBy(() -> companyDataService.createCompany(new CompanyCreateRequest(
            "Broken Co",
            "Technology",
            "https://broken.example.com",
            null,
            null,
            null,
            "HUGE"
        )))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid company size");
    }
}
