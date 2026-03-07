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

/**
 * Mock implementation of ScoringService for tests.
 * Generates realistic scores without requiring real AI providers.
 */
@Service
@Primary
@ConditionalOnProperty(
    name = "prospectos.scoring.mock.enabled", 
    havingValue = "true"
)
public class MockScoringAIService implements ScoringService {

    private static final Logger log = LoggerFactory.getLogger(MockScoringAIService.class);
    private final Random random = new Random(42); // Fixed seed for consistent tests

    public ScoringResult scoreCompany(Company company, ICP icp) {
        log.info("Mock AI calculating score: {}", company.getName());
        
        ScoringResult result = generateMockScore(company, icp);
        
        log.info("   Mock Score calculated: {} ({}) - {}", 
            result.score(), 
            result.priority(), 
            company.getName()
        );
        
        return result;
    }

    private ScoringResult generateMockScore(Company company, ICP icp) {
        int baseScore = 60;
        
        // Industry-based scoring
        if (company.getIndustry() != null) {
            String industry = company.getIndustry().toLowerCase();
            if (industry.contains("fintech")) baseScore += 20;
            if (industry.contains("technology")) baseScore += 15;
            if (industry.contains("saas")) baseScore += 15;
            if (industry.contains("agribusiness")) baseScore += 10;
            if (industry.contains("consulting")) baseScore += 8;
            if (industry.contains("edtech")) baseScore += 12;
        }
        
        // Company name patterns
        if (company.getName() != null) {
            String name = company.getName().toLowerCase();
            if (name.contains("tech")) baseScore += 10;
            if (name.contains("startup")) baseScore += 15;
            if (name.contains("ai") || name.contains("digital")) baseScore += 8;
        }
        
        // Location bonus
        if (company.getLocation() != null) {
            String location = company.getLocation().toLowerCase();
            if (location.contains("são paulo") || location.contains("sp")) baseScore += 10;
            if (location.contains("rio de janeiro") || location.contains("rj")) baseScore += 8;
            if (location.contains("belo horizonte")) baseScore += 6;
        }
        
        // Add some controlled randomness
        int variation = random.nextInt(21) - 10; // -10 to +10
        baseScore += variation;
        
        // Clamp score between 45-95
        int finalScore = Math.max(45, Math.min(95, baseScore));
        
        // Determine priority based on score
        PriorityLevel priority;
        if (finalScore >= 80) {
            priority = PriorityLevel.HOT;
        } else if (finalScore >= 65) {
            priority = PriorityLevel.WARM;
        } else {
            priority = PriorityLevel.COLD;
        }
        
        // Generate breakdown
        Map<String, Integer> breakdown = Map.of(
            "icpFit", Math.min(30, finalScore * 30 / 100),
            "signals", Math.min(25, finalScore * 25 / 100),
            "companySize", Math.min(20, finalScore * 20 / 100),
            "timing", Math.min(15, finalScore * 15 / 100),
            "accessibility", Math.min(10, finalScore * 10 / 100)
        );
        
        String reasoning = generateReasoning(company, finalScore, priority);
        String recommendation = generateRecommendation(priority);
        
        return new ScoringResult(finalScore, priority, reasoning, breakdown, recommendation);
    }
    
    private String generateReasoning(Company company, int score, PriorityLevel priority) {
        StringBuilder reasoning = new StringBuilder();
        
        // Priority prefix
        reasoning.append(priority.name()).append(" PRIORITY: ");
        
        // Industry analysis
        if (company.getIndustry() != null) {
            String industry = company.getIndustry().toLowerCase();
            if (industry.contains("fintech") || industry.contains("tech")) {
                reasoning.append("Strong tech industry fit. ");
            } else if (industry.contains("agribusiness")) {
                reasoning.append("Traditional sector with modernization potential. ");
            } else {
                reasoning.append("Industry analysis shows good potential. ");
            }
        }
        
        // Score-based reasoning
        if (score >= 80) {
            reasoning.append("High ICP alignment, immediate outreach recommended.");
        } else if (score >= 65) {
            reasoning.append("Good potential, schedule follow-up within 1-2 weeks.");
        } else {
            reasoning.append("Moderate fit, consider for nurture campaigns.");
        }
        
        // Location context
        if (company.getLocation() != null && company.getLocation().contains("São Paulo")) {
            reasoning.append(" Located in Brazil's tech hub.");
        }
        
        return reasoning.toString();
    }
    
    private String generateRecommendation(PriorityLevel priority) {
        return switch (priority) {
            case HOT -> "Prioritize immediate outreach. Strong alignment with ICP criteria.";
            case WARM -> "Schedule follow-up within 2 weeks. Good potential for conversion.";
            case COLD -> "Add to nurture campaign. Monitor for engagement signals.";
            case IGNORE -> "Low priority. Consider for future re-evaluation.";
        };
    }
}