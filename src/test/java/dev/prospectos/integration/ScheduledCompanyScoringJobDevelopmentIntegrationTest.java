package dev.prospectos.integration;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.infrastructure.service.scoring.CompanyScoringService;
import dev.prospectos.infrastructure.service.scoring.ScheduledCompanyScoringJob;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ScheduledCompanyScoringJobDevelopmentIntegrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(PropertyPlaceholderAutoConfiguration.class))
        .withBean(CompanyDataService.class, () -> mock(CompanyDataService.class))
        .withBean(CompanyScoringService.class, () -> mock(CompanyScoringService.class))
        .withBean(ScheduledCompanyScoringJob.class);

    @Test
    void registersBeanOnlyForDevelopmentProfileWhenScoringIsEnabled() {
        contextRunner
            .withPropertyValues(
                "spring.profiles.active=development",
                "prospectos.scoring.enabled=true",
                "prospectos.scoring.icp-id=7"
            )
            .run(context -> assertThat(context).hasSingleBean(ScheduledCompanyScoringJob.class));

        contextRunner
            .withPropertyValues(
                "spring.profiles.active=test",
                "prospectos.scoring.enabled=true",
                "prospectos.scoring.icp-id=7"
            )
            .run(context -> assertThat(context).doesNotHaveBean(ScheduledCompanyScoringJob.class));
    }

    @Test
    void scoreAllCompaniesSkipsWhenIcpIdIsMissing() {
        CompanyDataService companyDataService = mock(CompanyDataService.class);
        CompanyScoringService scoringService = mock(CompanyScoringService.class);
        ScheduledCompanyScoringJob job = new ScheduledCompanyScoringJob(companyDataService, scoringService, "");

        job.scoreAllCompanies();

        verifyNoInteractions(companyDataService, scoringService);
    }

    @Test
    void scoreAllCompaniesSkipsWhenIcpIdIsInvalid() {
        CompanyDataService companyDataService = mock(CompanyDataService.class);
        CompanyScoringService scoringService = mock(CompanyScoringService.class);
        ScheduledCompanyScoringJob job = new ScheduledCompanyScoringJob(companyDataService, scoringService, "abc");

        job.scoreAllCompanies();

        verifyNoInteractions(companyDataService, scoringService);
    }

    @Test
    void scoreAllCompaniesSkipsWhenNoCompaniesExist() {
        CompanyDataService companyDataService = mock(CompanyDataService.class);
        CompanyScoringService scoringService = mock(CompanyScoringService.class);
        when(companyDataService.findAllCompanies()).thenReturn(List.of());
        ScheduledCompanyScoringJob job = new ScheduledCompanyScoringJob(companyDataService, scoringService, "9");

        job.scoreAllCompanies();

        verify(companyDataService).findAllCompanies();
        verifyNoInteractions(scoringService);
    }

    @Test
    void scoreAllCompaniesContinuesAfterOneFailure() {
        CompanyDataService companyDataService = mock(CompanyDataService.class);
        CompanyScoringService scoringService = mock(CompanyScoringService.class);
        when(companyDataService.findAllCompanies()).thenReturn(List.of(
            company(1L, "First"),
            company(2L, "Second"),
            company(3L, "Third")
        ));
        doThrow(new RuntimeException("boom")).when(scoringService).scoreCompany(2L, 5L);
        ScheduledCompanyScoringJob job = new ScheduledCompanyScoringJob(companyDataService, scoringService, "5");

        job.scoreAllCompanies();

        verify(scoringService).scoreCompany(1L, 5L);
        verify(scoringService).scoreCompany(2L, 5L);
        verify(scoringService).scoreCompany(3L, 5L);
        verify(scoringService, times(3)).scoreCompany(anyLong(), anyLong());
    }

    private CompanyDTO company(Long id, String name) {
        return new CompanyDTO(
            id,
            name,
            "Technology",
            "https://" + name.toLowerCase() + ".example.com",
            "Test company",
            50,
            "Sao Paulo, BR",
            null
        );
    }
}
