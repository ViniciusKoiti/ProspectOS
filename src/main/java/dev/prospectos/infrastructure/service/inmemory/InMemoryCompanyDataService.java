package dev.prospectos.infrastructure.service.inmemory;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.request.CompanyCreateRequest;
import dev.prospectos.api.dto.request.CompanyUpdateRequest;
import dev.prospectos.infrastructure.service.discovery.CompanyVectorReindexRequested;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Profile({"test", "development"})
public class InMemoryCompanyDataService implements CompanyDataService {
    private final InMemoryCoreDataStore store;
    private final ApplicationEventPublisher eventPublisher;
    private final InMemoryCompanyDtoFactory dtoFactory;
    private final InMemoryWebsiteMatcher websiteMatcher;
    public InMemoryCompanyDataService(InMemoryCoreDataStore store, ApplicationEventPublisher eventPublisher) {
        this.store = store;
        this.eventPublisher = eventPublisher;
        this.dtoFactory = new InMemoryCompanyDtoFactory();
        this.websiteMatcher = new InMemoryWebsiteMatcher();
    }
    @Override
    public CompanyDTO findCompany(Long companyId) { return store.companies().get(companyId); }
    @Override
    public CompanyDTO findByWebsite(String website) {
        String targetDomain = websiteMatcher.extractDomainOrNull(website);
        if (targetDomain == null) {
            return null;
        }
        return store.companies().values().stream()
            .filter(company -> company.website() != null)
            .filter(company -> websiteMatcher.hasSameDomain(company.website(), targetDomain))
            .findFirst()
            .orElse(null);
    }
    @Override
    public List<CompanyDTO> findAllCompanies() { return List.copyOf(store.companies().values()); }
    @Override
    public CompanyDTO createCompany(CompanyCreateRequest request) {
        long companyId = store.nextCompanyId();
        CompanyDTO company = dtoFactory.fromCreateRequest(companyId, request);
        store.companies().put(companyId, company);
        publishReindex(companyId);
        return company;
    }
    @Override
    public CompanyDTO updateCompany(Long companyId, CompanyUpdateRequest request) {
        if (!store.companies().containsKey(companyId)) {
            return null;
        }
        CompanyDTO existing = store.companies().get(companyId);
        CompanyDTO company = dtoFactory.fromUpdateRequest(companyId, request, existing != null ? existing.score() : null);
        store.companies().put(companyId, company);
        publishReindex(companyId);
        return company;
    }
    @Override
    public boolean deleteCompany(Long companyId) {
        boolean deleted = store.companies().remove(companyId) != null;
        if (deleted) {
            publishReindex(companyId);
        }
        return deleted;
    }
    @Override
    public void updateCompanyScore(Long companyId, ScoreDTO score) {
        if (!store.companies().containsKey(companyId)) {
            throw new IllegalArgumentException("Company not found: " + companyId);
        }
        store.companyScores().put(companyId, score);
        CompanyDTO existing = store.companies().get(companyId);
        if (existing != null) {
            store.companies().put(companyId, dtoFactory.withScore(existing, score));
        }
        publishReindex(companyId);
    }
    @Override
    public List<CompanyDTO> findCompaniesByICP(Long icpId) {
        List<Long> companyIds = store.icpCompanies().getOrDefault(icpId, List.of());
        return companyIds.stream().map(store.companies()::get).filter(Objects::nonNull).toList();
    }
    private void publishReindex(Long companyId) {
        eventPublisher.publishEvent(new CompanyVectorReindexRequested(companyId));
    }
}
