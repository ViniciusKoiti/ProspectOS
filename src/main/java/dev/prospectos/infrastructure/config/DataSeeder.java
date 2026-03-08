package dev.prospectos.infrastructure.config;

import java.util.List;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.request.CompanyCreateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Seeds the database with demonstration data for mock and development profiles.
 */
@Component
@Profile({"development", "test"})
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final CompanyDataService companyService;
    private final ICPDataService icpService;
    private final DataSeederScoreCalculator scoreCalculator;

    public DataSeeder(CompanyDataService companyService, ICPDataService icpService) {
        this.companyService = companyService;
        this.icpService = icpService;
        this.scoreCalculator = new DataSeederScoreCalculator();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedDatabase() {
        log.info("Starting data seeding process for demo...");
        seedIcps();
        seedCompanies();
        assignRealisticScores();
        log.info("Database seeded successfully with {} companies and {} ICPs", companyService.findAllCompanies().size(), icpService.findAllICPs().size());
    }

    private void seedIcps() {
        DataSeederIcpRequests.requests().forEach(icpService::createICP);
    }

    private void seedCompanies() {
        for (CompanyCreateRequest request : DataSeederCompanyRequests.requests()) {
            createCompany(request);
        }
    }

    private void createCompany(CompanyCreateRequest request) {
        try {
            companyService.createCompany(request);
        } catch (Exception e) {
            log.warn("Failed to create company {}: {}", request.name(), e.getMessage());
        }
    }

    private void assignRealisticScores() {
        List<CompanyDTO> companies = companyService.findAllCompanies();
        for (CompanyDTO company : companies) {
            companyService.updateCompanyScore(company.id(), scoreCalculator.calculateFor(company));
        }
    }
}
