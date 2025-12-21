package dev.prospectos.core.api;

import dev.prospectos.core.api.dto.CompanyDTO;
import dev.prospectos.core.api.dto.ScoreDTO;

import java.util.List;

/**
 * Public interface for Company data access across modules.
 */
public interface CompanyDataService {

    CompanyDTO findCompany(Long companyId);

    void updateCompanyScore(Long companyId, ScoreDTO score);

    List<CompanyDTO> findCompaniesByICP(Long icpId);
}
