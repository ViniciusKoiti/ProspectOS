package dev.prospectos.core.api;

import dev.prospectos.core.api.dto.CompanyDTO;
import dev.prospectos.core.api.dto.ICPDto;
import dev.prospectos.core.api.dto.ScoreDTO;

/**
 * Interface pública do módulo Core
 * Permite que outros módulos acessem dados sem depender das entidades internas
 */
public interface CoreDataService {
    
    /**
     * Busca dados de uma empresa por ID
     */
    CompanyDTO findCompany(Long companyId);
    
    /**
     * Busca dados de um ICP por ID
     */
    ICPDto findICP(Long icpId);
    
    /**
     * Atualiza o score de uma empresa
     */
    void updateCompanyScore(Long companyId, ScoreDTO score);
    
    /**
     * Busca todas as empresas que correspondem a um ICP
     */
    java.util.List<CompanyDTO> findCompaniesByICP(Long icpId);
}