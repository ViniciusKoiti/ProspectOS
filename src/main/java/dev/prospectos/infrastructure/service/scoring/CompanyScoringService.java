package dev.prospectos.infrastructure.service.scoring;

import dev.prospectos.ai.dto.PriorityLevel;
import dev.prospectos.ai.dto.ScoringResult;
import dev.prospectos.ai.service.ScoringAIService;
import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.mapper.CompanyMapper;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyScoringService {

    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 100;
    private static final String DEFAULT_PRIORITY = "COLD";
    private static final String FALLBACK_PRIORITY = "IGNORE";

    private final ScoringAIService scoringAIService;
    private final CompanyDataService companyDataService;
    private final ICPDataService icpDataService;

    public CompanyScoringService(
        ScoringAIService scoringAIService,
        CompanyDataService companyDataService,
        ICPDataService icpDataService
    ) {
        this.scoringAIService = scoringAIService;
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

        Company company = CompanyMapper.toDomain(companyDTO);
        ICP icp = toDomainICP(icpDTO);

        ScoreDTO score = scoreCompanySafely(company, icp);
        companyDataService.updateCompanyScore(companyId, score);

        return score;
    }

    /**
     * Scores a company candidate (preview mode - no persistence).
     * This method calculates score without requiring the company to be persisted.
     * 
     * @param company Company domain object (can be transient)
     * @param icp ICP domain object
     * @return ScoreDTO with calculated score
     */
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
            ScoringResult result = scoringAIService.scoreCompany(company, icp);
            return mapScore(result);
        } catch (Exception e) {
            return fallbackScore("AI scoring failed: " + e.getMessage());
        }
    }

    private ScoreDTO mapScore(ScoringResult result) {
        if (result == null) {
            return fallbackScore("AI scoring failed: empty result");
        }

        int boundedScore = clamp(result.score());
        String priority = resolvePriority(result.priority());
        String reasoning = result.reasoning() == null ? "AI scoring completed" : result.reasoning();

        return new ScoreDTO(boundedScore, priority, reasoning);
    }

    private int clamp(int score) {
        if (score < MIN_SCORE) {
            return MIN_SCORE;
        }
        if (score > MAX_SCORE) {
            return MAX_SCORE;
        }
        return score;
    }

    private String resolvePriority(PriorityLevel priority) {
        if (priority == null) {
            return DEFAULT_PRIORITY;
        }
        return priority.name();
    }

    private ScoreDTO fallbackScore(String message) {
        String safeMessage = message == null || message.isBlank() ? "AI scoring failed" : message;
        return new ScoreDTO(MIN_SCORE, FALLBACK_PRIORITY, safeMessage);
    }
}
