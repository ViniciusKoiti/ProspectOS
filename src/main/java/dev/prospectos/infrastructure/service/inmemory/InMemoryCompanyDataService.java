package dev.prospectos.infrastructure.service.inmemory;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.request.CompanyCreateRequest;
import dev.prospectos.api.dto.request.CompanyUpdateRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * In-memory Company data service for demo and test profiles.
 */
@Service
@Profile({"demo", "test"})
public class InMemoryCompanyDataService implements CompanyDataService {

    private final InMemoryCoreDataStore store;

    public InMemoryCompanyDataService(InMemoryCoreDataStore store) {
        this.store = store;
    }

    @Override
    public CompanyDTO findCompany(Long companyId) {
        return store.companies().get(companyId);
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
            formatLocation(request.country(), request.city())
        );
        store.companies().put(companyId, company);
        return company;
    }

    @Override
    public CompanyDTO updateCompany(Long companyId, CompanyUpdateRequest request) {
        if (!store.companies().containsKey(companyId)) {
            return null;
        }
        CompanyDTO company = new CompanyDTO(
            companyId,
            request.name(),
            request.industry(),
            request.website(),
            request.description(),
            null,
            formatLocation(request.country(), request.city())
        );
        store.companies().put(companyId, company);
        return company;
    }

    @Override
    public boolean deleteCompany(Long companyId) {
        return store.companies().remove(companyId) != null;
    }

    @Override
    public void updateCompanyScore(Long companyId, ScoreDTO score) {
        if (!store.companies().containsKey(companyId)) {
            throw new IllegalArgumentException("Company not found: " + companyId);
        }
        store.companyScores().put(companyId, score);
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
}
