package dev.prospectos.infrastructure.service.jpa;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.domain.Score;
import dev.prospectos.core.repository.CompanyDomainRepository;
import dev.prospectos.core.repository.ICPDomainRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Repository-backed Company data service for non-test profiles.
 */
@Service
@Profile("!demo & !test")
public class CompanyDataServiceJpa implements CompanyDataService {

    private final CompanyDomainRepository companyRepository;
    private final ICPDomainRepository icpRepository;

    public CompanyDataServiceJpa(CompanyDomainRepository companyRepository, ICPDomainRepository icpRepository) {
        this.companyRepository = companyRepository;
        this.icpRepository = icpRepository;
    }

    @Override
    public CompanyDTO findCompany(Long companyId) {
        return findCompanyByExternalId(companyId)
            .map(this::toDTO)
            .orElse(null);
    }

    @Override
    public void updateCompanyScore(Long companyId, ScoreDTO score) {
        Company company = findCompanyByExternalId(companyId)
            .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));
        company.updateScore(Score.of(score.value()), score.reasoning());
        companyRepository.save(company);
    }

    @Override
    public List<CompanyDTO> findCompaniesByICP(Long icpId) {
        Optional<ICP> icp = findICPByExternalId(icpId);
        if (icp.isEmpty()) {
            return List.of();
        }

        List<String> industries = icp.get().getIndustries();
        if (industries == null || industries.isEmpty()) {
            return List.of();
        }

        return industries.stream()
            .flatMap(industry -> companyRepository.findByIndustry(industry).stream())
            .collect(Collectors.toMap(
                company -> company.getId().getMostSignificantBits(),
                this::toDTO,
                (existing, replacement) -> existing
            ))
            .values()
            .stream()
            .toList();
    }

    private Optional<Company> findCompanyByExternalId(Long externalId) {
        if (externalId == null) {
            return Optional.empty();
        }
        return companyRepository.findAll()
            .stream()
            .filter(company -> matchesExternalId(company.getId(), externalId))
            .findFirst();
    }

    private Optional<ICP> findICPByExternalId(Long externalId) {
        if (externalId == null) {
            return Optional.empty();
        }
        return icpRepository.findAll()
            .stream()
            .filter(icp -> matchesExternalId(icp.getId(), externalId))
            .findFirst();
    }

    private boolean matchesExternalId(UUID id, Long externalId) {
        return id != null && id.getMostSignificantBits() == externalId;
    }

    private CompanyDTO toDTO(Company company) {
        return new CompanyDTO(
            company.getId().getMostSignificantBits(),
            company.getName(),
            company.getIndustry(),
            company.getWebsite() != null ? company.getWebsite().getUrl() : null,
            company.getDescription(),
            null,
            company.getLocation()
        );
    }
}
