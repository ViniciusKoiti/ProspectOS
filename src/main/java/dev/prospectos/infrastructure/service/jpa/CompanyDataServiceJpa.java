package dev.prospectos.infrastructure.service.jpa;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.dto.CompanyContactDTO;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.request.CompanyCreateRequest;
import dev.prospectos.api.dto.request.CompanyUpdateRequest;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.Score;
import dev.prospectos.core.repository.CompanyDomainRepository;
import dev.prospectos.core.repository.ICPDomainRepository;
import dev.prospectos.infrastructure.service.discovery.CompanyVectorReindexRequested;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @Profile({"development", "production", "test-pg"}) @Transactional(readOnly = true)
public class CompanyDataServiceJpa implements CompanyDataService {
    private final CompanyDomainRepository companyRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CompanyJpaDtoMapper dtoMapper = new CompanyJpaDtoMapper();
    private final CompanyJpaProfileUpdater profileUpdater = new CompanyJpaProfileUpdater();
    private final CompanyJpaQuerySupport querySupport;
    private final CompanyJpaContactSupport contactSupport = new CompanyJpaContactSupport();

    public CompanyDataServiceJpa(CompanyDomainRepository companyRepository, ICPDomainRepository icpRepository, ApplicationEventPublisher eventPublisher) {
        this.companyRepository = companyRepository;
        this.eventPublisher = eventPublisher;
        this.querySupport = new CompanyJpaQuerySupport(companyRepository, icpRepository, dtoMapper);
    }

    @Override public CompanyDTO findCompany(Long companyId) { return querySupport.findCompany(companyId); }
    @Override public CompanyDTO findByWebsite(String website) { return querySupport.findByWebsite(website); }
    @Override public List<CompanyDTO> findAllCompanies() { return querySupport.findAllCompanies(); }
    @Override public List<CompanyDTO> findCompaniesByICP(Long icpId) { return querySupport.findCompaniesByIcp(icpId); }
    @Override public List<CompanyContactDTO> findCompanyContacts(Long companyId) { return querySupport.findCompanyEntity(companyId).map(contactSupport::toDTOs).orElse(List.of()); }

    @Override @Transactional
    public CompanyDTO createCompany(CompanyCreateRequest request) {
        CompanyDTO created = dtoMapper.toDTO(companyRepository.save(profileUpdater.create(request)));
        return publishAndReturn(created);
    }

    @Override @Transactional
    public CompanyDTO updateCompany(Long companyId, CompanyUpdateRequest request) {
        Company company = querySupport.findCompanyEntity(companyId).orElse(null);
        if (company == null) return null;
        profileUpdater.update(company, request);
        return publishAndReturn(dtoMapper.toDTO(companyRepository.save(company)));
    }

    @Override @Transactional
    public boolean deleteCompany(Long companyId) {
        Company company = querySupport.findCompanyEntity(companyId).orElse(null);
        if (company == null) return false;
        companyRepository.delete(company);
        publishReindex(companyId);
        return true;
    }

    @Override @Transactional
    public void updateCompanyScore(Long companyId, ScoreDTO score) {
        Company company = findCompanyEntityOrThrow(companyId);
        company.updateScore(Score.of(score.value()), score.reasoning());
        companyRepository.save(company);
        publishReindex(companyId);
    }

    @Override @Transactional
    public void addCompanyContactEmails(Long companyId, List<String> emails) {
        Company company = findCompanyEntityOrThrow(companyId);
        if (!contactSupport.addValidUniqueEmails(company, emails)) return;
        companyRepository.save(company);
        publishReindex(companyId);
    }

    private Company findCompanyEntityOrThrow(Long companyId) {
        return querySupport.findCompanyEntity(companyId).orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));
    }

    private CompanyDTO publishAndReturn(CompanyDTO company) {
        publishReindex(company.id());
        return company;
    }

    private void publishReindex(Long companyId) { eventPublisher.publishEvent(new CompanyVectorReindexRequested(companyId)); }
}
