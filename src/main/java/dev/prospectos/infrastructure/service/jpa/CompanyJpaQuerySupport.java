package dev.prospectos.infrastructure.service.jpa;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.domain.Website;
import dev.prospectos.core.repository.CompanyDomainRepository;
import dev.prospectos.core.repository.ICPDomainRepository;

final class CompanyJpaQuerySupport {

    private final CompanyDomainRepository companyRepository;
    private final ICPDomainRepository icpRepository;
    private final CompanyJpaDtoMapper dtoMapper;

    CompanyJpaQuerySupport(
        CompanyDomainRepository companyRepository,
        ICPDomainRepository icpRepository,
        CompanyJpaDtoMapper dtoMapper
    ) {
        this.companyRepository = companyRepository;
        this.icpRepository = icpRepository;
        this.dtoMapper = dtoMapper;
    }

    CompanyDTO findCompany(Long companyId) {
        return findCompanyEntity(companyId).map(dtoMapper::toDTO).orElse(null);
    }

    CompanyDTO findByWebsite(String website) {
        if (website == null || website.isBlank()) {
            return null;
        }
        try {
            String domain = Website.of(website).getDomain();
            return companyRepository.findByWebsiteDomain(domain).stream().findFirst().map(dtoMapper::toDTO).orElse(null);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    List<CompanyDTO> findAllCompanies() {
        return companyRepository.findAll().stream().map(dtoMapper::toDTO).toList();
    }

    List<CompanyDTO> findCompaniesByIcp(Long icpId) {
        Optional<ICP> icp = findIcpEntity(icpId);
        if (icp.isEmpty() || icp.get().getIndustries() == null || icp.get().getIndustries().isEmpty()) {
            return List.of();
        }
        return icp.get().getIndustries().stream()
            .flatMap(industry -> companyRepository.findByIndustry(industry).stream())
            .collect(Collectors.toMap(
                company -> company.getExternalId(),
                dtoMapper::toDTO,
                (existing, replacement) -> existing
            ))
            .values()
            .stream()
            .toList();
    }

    Optional<Company> findCompanyEntity(Long externalId) {
        return externalId == null ? Optional.empty() : companyRepository.findByExternalId(externalId);
    }

    private Optional<ICP> findIcpEntity(Long externalId) {
        return externalId == null ? Optional.empty() : icpRepository.findByExternalId(externalId);
    }
}

