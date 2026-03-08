package dev.prospectos.infrastructure.service.scoring;

import dev.prospectos.ai.dto.ScoringResult;
import dev.prospectos.ai.service.ScoringService;
import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.mapper.CompanyMapper;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CompanyScoringService {
    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 100;
    private static final String DEFAULT_PRIORITY = "COLD";
    private static final String FALLBACK_PRIORITY = "IGNORE";

    private final ScoringService scoringService;
    private final CompanyDataService companyDataService;
    private final ICPDataService icpDataService;
    public CompanyScoringService(ScoringService scoringService, CompanyDataService companyDataService,
                                 ICPDataService icpDataService) {
        this.scoringService = scoringService;
        this.companyDataService = companyDataService;
        this.icpDataService = icpDataService;
    }

    public ScoreDTO scoreCompany(Long companyId, Long icpId) {
        if (companyId == null || icpId == null) {
            throw new IllegalArgumentException("Company ID and ICP ID are required");
        }
        CompanyDTO companyDTO = companyDataService.findCompany(companyId);
        if (companyDTO == null) {
            throw new IllegalArgumentException("Company not found");
        }
        ICPDto icpDTO = icpDataService.findICP(icpId);
        if (icpDTO == null) {
            throw new IllegalArgumentException("ICP not found");
        }
        ScoreDTO score = scoreCompanySafely(CompanyMapper.toDomain(companyDTO), toDomainICP(icpDTO));
        companyDataService.updateCompanyScore(companyId, score);
        return score;
    }

    public ScoreDTO scoreCandidate(Company company, ICP icp) {
        if (company == null) {
            throw new IllegalArgumentException("Company cannot be null");
        }
        if (icp == null) {
            throw new IllegalArgumentException("ICP cannot be null");
        }
        return scoreCompanySafely(company, icp);
    }

    private ICP toDomainICP(ICPDto icpDTO) {
        return ICP.create(
            icpDTO.name(),
            icpDTO.description(),
            icpDTO.targetIndustries() != null ? icpDTO.targetIndustries() : List.of(),
            icpDTO.regions() != null ? icpDTO.regions() : List.of(),
            icpDTO.targetRoles() != null ? icpDTO.targetRoles() : List.of(),
            icpDTO.interestTheme()
        );
    }

    private ScoreDTO scoreCompanySafely(Company company, ICP icp) {
        try {
            log.debug("Starting scoring for company: {} with ICP: {}", company.getName(), icp.getName());
            ScoringResult result = scoringService.scoreCompany(company, icp);
            log.debug("Scoring completed. Result: {}", result);
            return mapScore(result);
        } catch (Exception e) {
            log.error("Scoring failed for company: {}", company.getName(), e);
            return fallbackScore("AI scoring failed: " + e.getMessage());
        }
    }

    private ScoreDTO mapScore(ScoringResult result) {
        if (result == null) {
            return fallbackScore("AI scoring failed: empty result");
        }
        int boundedScore = result.score() < MIN_SCORE ? MIN_SCORE : Math.min(result.score(), MAX_SCORE);
        String priority = result.priority() == null ? DEFAULT_PRIORITY : result.priority().name();
        String reasoning = result.reasoning() == null ? "AI scoring completed" : result.reasoning();
        return new ScoreDTO(boundedScore, priority, reasoning);
    }

    private ScoreDTO fallbackScore(String message) {
        String safeMessage = message == null || message.isBlank() ? "AI scoring failed" : message;
        return new ScoreDTO(MIN_SCORE, FALLBACK_PRIORITY, safeMessage);
    }
}
