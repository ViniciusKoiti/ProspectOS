package dev.prospectos.infrastructure.service.jpa;

import dev.prospectos.core.domain.ExternalIdPolicy;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.repository.ICPDomainRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Normalizes ICP external IDs that are not safe for JavaScript clients.
 */
@Slf4j
@Component
@Profile("development")
public class DevelopmentIcpExternalIdNormalizer implements ApplicationRunner {

    private final ICPDomainRepository icpRepository;

    public DevelopmentIcpExternalIdNormalizer(ICPDomainRepository icpRepository) {
        this.icpRepository = icpRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int normalized = normalizeIcps();
        if (normalized > 0) {
            log.info("Normalized unsafe ICP external IDs in development: {}", normalized);
        }
    }

    private int normalizeIcps() {
        List<ICP> icps = icpRepository.findAll();
        Set<Long> usedIds = ExternalIdNormalizationSupport.collectSafeIds(icps.stream().map(ICP::getExternalId).toList());
        long nextId = ExternalIdNormalizationSupport.nextStart(usedIds);
        int normalized = 0;

        for (ICP icp : icps) {
            Long currentId = icp.getExternalId();
            if (ExternalIdPolicy.isSafe(currentId)) {
                continue;
            }
            long replacementId = ExternalIdNormalizationSupport.nextAvailable(usedIds, nextId);
            icp.normalizeExternalId(replacementId);
            icpRepository.save(icp);
            usedIds.add(replacementId);
            nextId = replacementId + 1;
            normalized++;
        }

        return normalized;
    }
}
