package dev.prospectos.infrastructure.service.jpa;

import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.repository.ICPDomainRepository;
import dev.prospectos.infrastructure.config.LeadSearchProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds a default ICP for development profile when configured and missing.
 */
@Slf4j
@Component
@Profile("development")
public class DevelopmentDefaultIcpSeeder implements ApplicationRunner {

    private static final String DEFAULT_ICP_NAME = "Default";

    private final ICPDomainRepository icpRepository;
    private final LeadSearchProperties leadSearchProperties;

    public DevelopmentDefaultIcpSeeder(
        ICPDomainRepository icpRepository,
        LeadSearchProperties leadSearchProperties
    ) {
        this.icpRepository = icpRepository;
        this.leadSearchProperties = leadSearchProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        Long defaultIcpId = leadSearchProperties.defaultIcpId();
        if (defaultIcpId == null) {
            log.debug("Skipping development ICP seed because prospectos.leads.default-icp-id is not configured.");
            return;
        }

        if (icpRepository.findByExternalId(defaultIcpId).isPresent()) {
            return;
        }

        ICP defaultIcp = ICP.createWithExternalId(
            defaultIcpId,
            DEFAULT_ICP_NAME,
            "Default ICP created automatically for development profile",
            List.of("Software", "Technology", "SaaS"),
            List.of("Brazil"),
            List.of("CTO", "Head of Engineering"),
            "Operational efficiency and growth"
        );

        icpRepository.save(defaultIcp);
        log.info("Created development default ICP with externalId={}", defaultIcpId);
    }
}
