package dev.prospectos.infrastructure.service.jpa;

import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ExternalIdPolicy;
import dev.prospectos.core.repository.CompanyDomainRepository;
import dev.prospectos.infrastructure.api.companies.SourceProvenance;
import dev.prospectos.infrastructure.jpa.SourceProvenanceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Normalizes company external IDs that are not safe for JavaScript clients.
 */
@Slf4j
@Component
@Profile("development")
public class DevelopmentCompanyExternalIdNormalizer implements ApplicationRunner {

    private final CompanyDomainRepository companyRepository;
    private final SourceProvenanceRepository sourceProvenanceRepository;

    public DevelopmentCompanyExternalIdNormalizer(
        CompanyDomainRepository companyRepository,
        SourceProvenanceRepository sourceProvenanceRepository
    ) {
        this.companyRepository = companyRepository;
        this.sourceProvenanceRepository = sourceProvenanceRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int normalized = normalizeCompanies();
        if (normalized > 0) {
            log.info("Normalized unsafe company external IDs in development: {}", normalized);
        }
    }

    private int normalizeCompanies() {
        List<Company> companies = companyRepository.findAll();
        Set<Long> usedIds = ExternalIdNormalizationSupport.collectSafeIds(companies.stream().map(Company::getExternalId).toList());
        long nextId = ExternalIdNormalizationSupport.nextStart(usedIds);
        int normalized = 0;

        for (Company company : companies) {
            Long currentId = company.getExternalId();
            if (ExternalIdPolicy.isSafe(currentId)) {
                continue;
            }
            long replacementId = ExternalIdNormalizationSupport.nextAvailable(usedIds, nextId);
            company.normalizeExternalId(replacementId);
            companyRepository.save(company);
            rewireSourceProvenance(currentId, replacementId);
            usedIds.add(replacementId);
            nextId = replacementId + 1;
            normalized++;
        }

        return normalized;
    }

    private void rewireSourceProvenance(Long oldId, long newId) {
        if (oldId == null || oldId.equals(newId)) {
            return;
        }
        List<SourceProvenance> provenance = sourceProvenanceRepository.findByCompanyExternalId(oldId);
        if (provenance.isEmpty()) {
            return;
        }
        provenance.forEach(record -> record.replaceCompanyExternalId(newId));
        sourceProvenanceRepository.saveAll(provenance);
    }
}
