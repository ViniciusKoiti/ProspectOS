package dev.prospectos.core.api;

import dev.prospectos.core.api.dto.CompanyDTO;
import dev.prospectos.core.api.dto.ICPDto;
import dev.prospectos.core.api.dto.ScoreDTO;

/**
 * Public interface for the Core module.
 * Allows other modules to access data without depending on internal entities.
 */
public interface CoreDataService {
    
    /**
     * Fetches company data by ID.
     */
    CompanyDTO findCompany(Long companyId);
    
    /**
     * Fetches ICP data by ID.
     */
    ICPDto findICP(Long icpId);
    
    /**
     * Updates a company score.
     */
    void updateCompanyScore(Long companyId, ScoreDTO score);
    
    /**
     * Fetches all companies that match an ICP.
     */
    java.util.List<CompanyDTO> findCompaniesByICP(Long icpId);
}
