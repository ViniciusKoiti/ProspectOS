package dev.prospectos.infrastructure.service.inmemory;

import java.time.Instant;
import java.util.List;

import dev.prospectos.api.dto.CompanyCandidateDTO;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.LeadResultDTO;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.SourceProvenanceDTO;
import dev.prospectos.api.mapper.CompanyMapper;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.util.LeadKeyGenerator;
import dev.prospectos.infrastructure.service.scoring.CompanyScoringService;

final class InMemoryLeadResultFactory {

    private final CompanyScoringService scoringService;

    InMemoryLeadResultFactory(CompanyScoringService scoringService) {
        this.scoringService = scoringService;
    }

    LeadResultDTO toLeadResult(CompanyDTO companyDto, String sourceName, ICP icp) {
        Company company = CompanyMapper.toDomain(companyDto);
        ScoreDTO score = scoringService.scoreCandidate(company, icp);

        CompanyCandidateDTO candidate = new CompanyCandidateDTO(
            companyDto.name(),
            companyDto.website(),
            companyDto.industry(),
            companyDto.description(),
            inferSizeFromEmployeeCount(companyDto.employeeCount()),
            companyDto.location(),
            List.of()
        );

        String leadKey = LeadKeyGenerator.generate(companyDto.website(), sourceName);
        SourceProvenanceDTO provenance = new SourceProvenanceDTO(sourceName, companyDto.website(), Instant.now());
        return new LeadResultDTO(candidate, score, provenance, leadKey);
    }

    private String inferSizeFromEmployeeCount(Integer employeeCount) {
        if (employeeCount == null) {
            return null;
        }
        if (employeeCount <= 10) {
            return "STARTUP";
        }
        if (employeeCount <= 50) {
            return "SMALL";
        }
        if (employeeCount <= 200) {
            return "MEDIUM";
        }
        if (employeeCount <= 1000) {
            return "LARGE";
        }
        return "ENTERPRISE";
    }
}
