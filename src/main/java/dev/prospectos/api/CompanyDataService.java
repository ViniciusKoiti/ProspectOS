package dev.prospectos.api;

import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ScoreDTO;

import java.util.List;

/**
 * Public interface for Company data access across modules.
 */
public interface CompanyDataService {

    CompanyDTO findCompany(Long companyId);

    void updateCompanyScore(Long companyId, ScoreDTO score);

    List<CompanyDTO> findCompaniesByICP(Long icpId);
}
