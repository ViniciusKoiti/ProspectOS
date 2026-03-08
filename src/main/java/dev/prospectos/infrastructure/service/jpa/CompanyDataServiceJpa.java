package dev.prospectos.infrastructure.service.jpa;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.request.CompanyCreateRequest;
import dev.prospectos.api.dto.request.CompanyUpdateRequest;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.Score;
import dev.prospectos.core.repository.CompanyDomainRepository;
import dev.prospectos.core.repository.ICPDomainRepository;
import dev.prospectos.infrastructure.service.discovery.CompanyVectorReindexRequested;

@Service
@Profile({"development", "production"})
public class CompanyDataServiceJpa implements CompanyDataService {
    private final CompanyDomainRepository companyRepository;
    private final ICPDomainRepository icpRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CompanyJpaDtoMapper dtoMapper;
    private final CompanyJpaProfileUpdater profileUpdater;
    private final CompanyJpaQuerySupport querySupport;

    public CompanyDataServiceJpa(
        CompanyDomainRepository companyRepository,
        ICPDomainRepository icpRepository,
        ApplicationEventPublisher eventPublisher
    ) {
        this.companyRepository = companyRepository;
        this.icpRepository = icpRepository;
        this.eventPublisher = eventPublisher;
        this.dtoMapper = new CompanyJpaDtoMapper();
        this.profileUpdater = new CompanyJpaProfileUpdater();
        this.querySupport = new CompanyJpaQuerySupport(companyRepository, icpRepository, dtoMapper);
    }
    @Override
    public CompanyDTO findCompany(Long companyId) {
        return querySupport.findCompany(companyId);
    }
    @Override
    public CompanyDTO findByWebsite(String website) {
        return querySupport.findByWebsite(website);
    }
    @Override
    public List<CompanyDTO> findAllCompanies() {
        return querySupport.findAllCompanies();
    }
    @Override
    public CompanyDTO createCompany(CompanyCreateRequest request) {
        Company company = profileUpdater.create(request);
        CompanyDTO created = dtoMapper.toDTO(companyRepository.save(company));
        publishReindex(created.id());
        return created;
    }
    @Override
    public CompanyDTO updateCompany(Long companyId, CompanyUpdateRequest request) {
        var existing = querySupport.findCompanyEntity(companyId);
        if (existing.isEmpty()) {
            return null;
        }
        Company company = existing.get();
        profileUpdater.update(company, request);
        CompanyDTO updated = dtoMapper.toDTO(companyRepository.save(company));
        publishReindex(updated.id());
        return updated;
    }
    @Override
    public boolean deleteCompany(Long companyId) {
        var existing = querySupport.findCompanyEntity(companyId);
        if (existing.isEmpty()) {
            return false;
        }
        companyRepository.delete(existing.get());
        publishReindex(companyId);
        return true;
    }
    @Override
    public void updateCompanyScore(Long companyId, ScoreDTO score) {
        Company company = querySupport.findCompanyEntity(companyId)
            .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));
        company.updateScore(Score.of(score.value()), score.reasoning());
        companyRepository.save(company);
        publishReindex(companyId);
    }
    @Override
    public List<CompanyDTO> findCompaniesByICP(Long icpId) {
        return querySupport.findCompaniesByIcp(icpId);
    }
    private void publishReindex(Long companyId) {
        eventPublisher.publishEvent(new CompanyVectorReindexRequested(companyId));
    }
}
