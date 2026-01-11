package dev.prospectos.infrastructure.service.scoring;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.dto.CompanyDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Profile("development")
@ConditionalOnProperty(prefix = "prospectos.scoring", name = "enabled", havingValue = "true")
public class ScheduledCompanyScoringJob {

    private final CompanyDataService companyDataService;
    private final CompanyScoringService companyScoringService;
    private final String icpIdProperty;

    public ScheduledCompanyScoringJob(
        CompanyDataService companyDataService,
        CompanyScoringService companyScoringService,
        @Value("${prospectos.scoring.icp-id:}") String icpIdProperty
    ) {
        this.companyDataService = companyDataService;
        this.companyScoringService = companyScoringService;
        this.icpIdProperty = icpIdProperty;
    }

    @Scheduled(cron = "${prospectos.scoring.cron:0 0 * * * *}")
    public void scoreAllCompanies() {
        Long icpId = resolveIcpId();
        if (icpId == null) {
            log.warn("Scoring job skipped: prospectos.scoring.icp-id not set");
            return;
        }

        List<CompanyDTO> companies = companyDataService.findAllCompanies();
        if (companies.isEmpty()) {
            log.info("Scoring job skipped: no companies found");
            return;
        }

        for (CompanyDTO company : companies) {
            try {
                companyScoringService.scoreCompany(company.id(), icpId);
            } catch (Exception e) {
                log.warn("Scoring failed for company {}: {}", company.id(), e.getMessage());
            }
        }
    }

    private Long resolveIcpId() {
        if (icpIdProperty == null || icpIdProperty.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(icpIdProperty.trim());
        } catch (NumberFormatException e) {
            log.warn("Invalid prospectos.scoring.icp-id: {}", icpIdProperty);
            return null;
        }
    }
}
