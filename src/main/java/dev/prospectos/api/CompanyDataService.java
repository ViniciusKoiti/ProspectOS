package dev.prospectos.api;

import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.request.CompanyCreateRequest;
import dev.prospectos.api.dto.request.CompanyUpdateRequest;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Public interface for Company data access across modules.
 */
public interface CompanyDataService {

    @Nullable
    CompanyDTO findCompany(Long companyId);

    @Nullable
    CompanyDTO findByWebsite(String website);

    List<CompanyDTO> findAllCompanies();

    CompanyDTO createCompany(CompanyCreateRequest request);

    @Nullable
    CompanyDTO updateCompany(Long companyId, CompanyUpdateRequest request);

    boolean deleteCompany(Long companyId);

    void updateCompanyScore(Long companyId, ScoreDTO score);

    List<CompanyDTO> findCompaniesByICP(Long icpId);
}
