package dev.prospectos.ai.service;

import dev.prospectos.ai.client.AIProvider;
import dev.prospectos.ai.dto.ScoringResult;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Scoring service using AI with structured output.
 */
@Slf4j
@Service
public class ScoringAIService {
    
    private final AIProvider aiProvider;
    
    public ScoringAIService(AIProvider aiProvider) {
        this.aiProvider = aiProvider;
    }
    
    /**
     * Calculates a company score (0-100) using AI.
     * Returns a structured object parsed automatically.
     */
    public ScoringResult scoreCompany(Company company, ICP icp) {
        log.info("AI calculating score: {}", company.getName());
        
        String prompt = String.format("""
                COMPANY:
                Name: %s
                Industry: %s
                Location: %s
                AI Analysis: %s
                Active Signals: %s
                
                TARGET ICP:
                Industries: %s
                Regions: %s
                Theme: %s
                
                TASK:
                Calculate the score (0-100) for this company based on the criteria:
                1. ICP fit (30 points)
                2. Interest signals (25 points)
                3. Company size and maturity (20 points)
                4. Timing and urgency (15 points)
                5. Contact accessibility (10 points)
                
                Return JSON with exactly this structure:
                {
                  "score": 75,
                  "priority": "HOT",
                  "reasoning": "Company X...",
                  "breakdown": {
                    "icpFit": 28,
                    "signals": 20,
                    "companySize": 15,
                    "timing": 12,
                    "accessibility": 8
                  },
                  "recommendation": "Prioritize immediate contact because..."
                }
                """,
                company.getName(),
                company.getIndustry(),
                company.getLocation(),
                company.getAiAnalysis() != null ? company.getAiAnalysis() : "N/A",
                company.hasActiveSignals(),
                String.join(", ", icp.getIndustries()),
                String.join(", ", icp.getRegions()),
                icp.getInterestTheme());
        
        // AI parses into ScoringResult automatically.
        ScoringResult result = aiProvider.calculateScore(prompt, ScoringResult.class);
        
        log.info("   Score calculated: {} ({}) - {}", 
            result.score(), 
            result.priority(),
            company.getName()
        );
        
        return result;
    }
}
