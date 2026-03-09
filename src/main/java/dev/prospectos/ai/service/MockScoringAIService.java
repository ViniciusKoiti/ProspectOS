package dev.prospectos.ai.service;

import dev.prospectos.ai.dto.PriorityLevel;
import dev.prospectos.ai.dto.ScoringResult;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;

@Service
@Primary
@ConditionalOnProperty(name = "prospectos.scoring.mock.enabled", havingValue = "true")
public class MockScoringAIService implements ScoringService {

    private static final Logger log = LoggerFactory.getLogger(MockScoringAIService.class);

    private final Random random = new Random(42);
    private final MockScoringNarrativeBuilder narrativeBuilder = new MockScoringNarrativeBuilder();

    public ScoringResult scoreCompany(Company company, ICP icp) {
        log.info("Mock AI calculating score: {}", company.getName());
        ScoringResult result = generateMockScore(company, icp);
        log.info("   Mock Score calculated: {} ({}) - {}", result.score(), result.priority(), company.getName());
        return result;
    }

    private ScoringResult generateMockScore(Company company, ICP icp) {
        int finalScore = Math.max(45, Math.min(95, baseScore(company) + (random.nextInt(21) - 10)));
        PriorityLevel priority = finalScore >= 80 ? PriorityLevel.HOT : finalScore >= 65 ? PriorityLevel.WARM : PriorityLevel.COLD;
        Map<String, Integer> breakdown = Map.of(
            "icpFit", Math.min(30, finalScore * 30 / 100),
            "signals", Math.min(25, finalScore * 25 / 100),
            "companySize", Math.min(20, finalScore * 20 / 100),
            "timing", Math.min(15, finalScore * 15 / 100),
            "accessibility", Math.min(10, finalScore * 10 / 100)
        );
        return new ScoringResult(
            finalScore,
            priority,
            narrativeBuilder.reasoning(company, finalScore, priority),
            breakdown,
            narrativeBuilder.recommendation(priority)
        );
    }

    private int baseScore(Company company) {
        int baseScore = 60;
        if (company.getIndustry() != null) {
            String industry = company.getIndustry().toLowerCase();
            if (industry.contains("fintech")) baseScore += 20;
            if (industry.contains("technology")) baseScore += 15;
            if (industry.contains("saas")) baseScore += 15;
            if (industry.contains("agribusiness")) baseScore += 10;
            if (industry.contains("consulting")) baseScore += 8;
            if (industry.contains("edtech")) baseScore += 12;
        }
        if (company.getName() != null) {
            String name = company.getName().toLowerCase();
            if (name.contains("tech")) baseScore += 10;
            if (name.contains("startup")) baseScore += 15;
            if (name.contains("ai") || name.contains("digital")) baseScore += 8;
        }
        if (company.getLocation() != null) {
            String location = company.getLocation().toLowerCase();
            if (location.contains("são paulo") || location.contains("sp")) baseScore += 10;
            if (location.contains("rio de janeiro") || location.contains("rj")) baseScore += 8;
            if (location.contains("belo horizonte")) baseScore += 6;
        }
        return baseScore;
    }
}
