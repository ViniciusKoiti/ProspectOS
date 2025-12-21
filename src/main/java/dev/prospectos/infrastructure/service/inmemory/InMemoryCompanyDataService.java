package dev.prospectos.infrastructure.service.inmemory;

import dev.prospectos.core.api.CompanyDataService;
import dev.prospectos.core.api.dto.CompanyDTO;
import dev.prospectos.core.api.dto.ScoreDTO;
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
}
