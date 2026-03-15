package dev.prospectos.infrastructure.service.inmemory;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.CompanyContactDTO;
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
@Profile("!test-pg & test")
public class InMemoryCompanyDataService implements CompanyDataService {
    private final InMemoryCoreDataStore store;
    private final ApplicationEventPublisher eventPublisher;
    private final InMemoryCompanyDtoFactory dtoFactory = new InMemoryCompanyDtoFactory();
    private final InMemoryWebsiteMatcher websiteMatcher = new InMemoryWebsiteMatcher();
    private final InMemoryCompanyContactSupport contactSupport = new InMemoryCompanyContactSupport();

    public InMemoryCompanyDataService(InMemoryCoreDataStore store, ApplicationEventPublisher eventPublisher) {
        this.store = store;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public CompanyDTO findCompany(Long companyId) { return store.companies().get(companyId); }
    @Override
    public CompanyDTO findByWebsite(String website) {
        String targetDomain = websiteMatcher.extractDomainOrNull(website);
        if (targetDomain == null) return null;
        return store.companies().values().stream().filter(c -> c.website() != null)
            .filter(c -> websiteMatcher.hasSameDomain(c.website(), targetDomain)).findFirst().orElse(null);
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
        CompanyDTO existing = store.companies().get(companyId);
        if (existing == null) return null;
        CompanyDTO updated = dtoFactory.fromUpdateRequest(companyId, request, existing);
        store.companies().put(companyId, updated);
        publishReindex(companyId);
        return updated;
    }
    @Override
    public boolean deleteCompany(Long companyId) {
        boolean deleted = store.companies().remove(companyId) != null;
        store.companyContacts().remove(companyId);
        if (deleted) publishReindex(companyId);
        return deleted;
    }
    @Override
    public void updateCompanyScore(Long companyId, ScoreDTO score) {
        CompanyDTO existing = store.companies().get(companyId);
        if (existing == null) throw new IllegalArgumentException("Company not found: " + companyId);
        store.companyScores().put(companyId, score);
        store.companies().put(companyId, dtoFactory.withScore(existing, score));
        publishReindex(companyId);
    }
    @Override
    public List<CompanyContactDTO> findCompanyContacts(Long companyId) { return contactSupport.findContacts(store, companyId); }
    @Override
    public void addCompanyContactEmails(Long companyId, List<String> emails) {
        CompanyDTO company = store.companies().get(companyId);
        if (company == null) throw new IllegalArgumentException("Company not found: " + companyId);
        if (!contactSupport.addValidUniqueEmails(store, companyId, emails)) return;
        store.companies().put(companyId, dtoFactory.withContacts(
            company, contactSupport.primaryEmail(store, companyId), contactSupport.contactCount(store, companyId)
        ));
        publishReindex(companyId);
    }
    @Override
    public List<CompanyDTO> findCompaniesByICP(Long icpId) {
        return store.icpCompanies().getOrDefault(icpId, List.of()).stream()
            .map(store.companies()::get).filter(Objects::nonNull).toList();
    }
    private void publishReindex(Long companyId) { eventPublisher.publishEvent(new CompanyVectorReindexRequested(companyId)); }
}
