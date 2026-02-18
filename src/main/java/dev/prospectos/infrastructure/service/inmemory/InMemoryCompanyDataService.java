package dev.prospectos.infrastructure.service.inmemory;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.request.CompanyCreateRequest;
import dev.prospectos.api.dto.request.CompanyUpdateRequest;
import dev.prospectos.core.domain.Website;
import dev.prospectos.infrastructure.service.discovery.CompanyVectorReindexRequested;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * In-memory Company data service for demo and test profiles.
 */
@Service
@Profile({"demo", "test", "mock"})
public class InMemoryCompanyDataService implements CompanyDataService {

    private final InMemoryCoreDataStore store;
    private final ApplicationEventPublisher eventPublisher;

    public InMemoryCompanyDataService(InMemoryCoreDataStore store, ApplicationEventPublisher eventPublisher) {
        this.store = store;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public CompanyDTO findCompany(Long companyId) {return store.companies().get(companyId);
    }

    @Override
    public CompanyDTO findByWebsite(String website) {
        if (website == null || website.isBlank()) {
            return null;
        }

        String targetDomain;
        try {
            targetDomain = Website.of(website).getDomain();
        } catch (IllegalArgumentException ex) {
            return null;
        }

        return store.companies().values().stream()
            .filter(company -> company.website() != null)
            .filter(company -> hasSameDomain(company.website(), targetDomain))
            .findFirst()
            .orElse(null);
    }

    @Override
    public List<CompanyDTO> findAllCompanies() {
        return List.copyOf(store.companies().values());
    }

    @Override
    public CompanyDTO createCompany(CompanyCreateRequest request) {
        long companyId = store.nextCompanyId();
        CompanyDTO company = new CompanyDTO(
            companyId,
            request.name(),
            request.industry(),
            request.website(),
            request.description(),
            null,
            formatLocation(request.country(), request.city()),
            null // score will be set separately via updateCompanyScore
        );
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
        CompanyDTO company = new CompanyDTO(
            companyId,
            request.name(),
            request.industry(),
            request.website(),
            request.description(),
            null,
            formatLocation(request.country(), request.city()),
            existing != null ? existing.score() : null // preserve existing score
        );
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
        
        // Update the CompanyDTO with the new score
        CompanyDTO existing = store.companies().get(companyId);
        if (existing != null) {
            CompanyDTO updated = new CompanyDTO(
                existing.id(),
                existing.name(),
                existing.industry(),
                existing.website(),
                existing.description(),
                existing.employeeCount(),
                existing.location(),
                score
            );
            store.companies().put(companyId, updated);
        }
        publishReindex(companyId);
    }

    @Override
    public List<CompanyDTO> findCompaniesByICP(Long icpId) {
        List<Long> companyIds = store.icpCompanies().getOrDefault(icpId, List.of());
        return companyIds.stream()
            .map(store.companies()::get)
            .filter(Objects::nonNull)
            .toList();
    }

    private String formatLocation(String country, String city) {
        if (city != null && country != null) {
            return city + ", " + country;
        }
        if (city != null) {
            return city;
        }
        if (country != null) {
            return country;
        }
        return null;
    }

    private boolean hasSameDomain(String website, String targetDomain) {
        try {
            return Website.of(website).getDomain().equalsIgnoreCase(targetDomain);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private void publishReindex(Long companyId) {
        eventPublisher.publishEvent(new CompanyVectorReindexRequested(companyId));
    }
}
