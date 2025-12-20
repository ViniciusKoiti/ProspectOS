package dev.prospectos.infrastructure.service;

import dev.prospectos.core.api.CoreDataService;
import dev.prospectos.core.api.dto.CompanyDTO;
import dev.prospectos.core.api.dto.ICPDto;
import dev.prospectos.core.api.dto.ScoreDTO;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.repository.CompanyDomainRepository;
import dev.prospectos.core.repository.ICPDomainRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Infrastructure implementation of CoreDataService.
 * Uses domain repositories to access data and converts to DTOs for external modules.
 */
@Service
public class CoreDataServiceImpl implements CoreDataService {

    private final CompanyDomainRepository companyRepository;
    private final ICPDomainRepository icpRepository;

    public CoreDataServiceImpl(CompanyDomainRepository companyRepository, ICPDomainRepository icpRepository) {
        this.companyRepository = companyRepository;
        this.icpRepository = icpRepository;
    }

    @Override
    public CompanyDTO findCompany(Long companyId) {
        // For now, return mock data since the domain uses UUID
        return CompanyDTO.createMock();
    }

    @Override
    public ICPDto findICP(Long icpId) {
        // For now, return mock data since the domain uses UUID
        return ICPDto.createMock();
    }

    @Override
    public void updateCompanyScore(Long companyId, ScoreDTO score) {
        // Mock implementation for now
        // In a real implementation, this would find the company and update its score
    }

    @Override
    public List<CompanyDTO> findCompaniesByICP(Long icpId) {
        // Mock implementation for now
        return List.of(CompanyDTO.createMock());
    }
    
    /**
     * Converts Company domain entity to DTO.
     */
    private CompanyDTO toDTO(Company company) {
        return new CompanyDTO(
            company.getId().getMostSignificantBits(), // Convert UUID to Long for now
            company.getName(),
            company.getIndustry(),
            company.getWebsite().getUrl(),
            company.getDescription(),
            company.getSize() != null ? 100 : 0, // Mock employee count based on size
            company.getLocation()
        );
    }
    
    /**
     * Converts ICP domain entity to DTO.
     */
    private ICPDto toDTO(ICP icp) {
        return new ICPDto(
            icp.getId().getMostSignificantBits(), // Convert UUID to Long for now
            icp.getName(),
            icp.getDescription(),
            icp.getIndustries(),
            List.of("Docker", "Kubernetes"), // Mock technologies for now
            50, // Mock min employee count
            500, // Mock max employee count
            icp.getTargetRoles()
        );
    }
}