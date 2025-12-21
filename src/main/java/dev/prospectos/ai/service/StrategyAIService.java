package dev.prospectos.ai.service;

import dev.prospectos.ai.client.AIProvider;
import dev.prospectos.ai.dto.StrategyRecommendation;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Outreach strategy service using AI.
 */
@Slf4j
@Service
public class StrategyAIService {
    
    private final AIProvider aiProvider;
    
    public StrategyAIService(AIProvider aiProvider) {
        this.aiProvider = aiProvider;
    }
    
    /**
     * Generates an outreach strategy recommendation.
     */
    public StrategyRecommendation recommendStrategy(Company company, ICP icp) {
        log.info("AI generating strategy: {}", company.getName());
        
        String prompt = String.format("""
                Analyze the company and recommend the best outreach strategy.
                
                COMPANY:
                Name: %s
                Industry: %s
                Size: %s
                Location: %s
                AI Analysis: %s
                Score: %s
                
                ICP:
                Theme: %s
                Target Roles: %s
                
                TASK:
                Based on the analysis, recommend the best outreach strategy.
                
                Return JSON with this exact structure:
                {
                  "channel": "email|linkedin|phone|event",
                  "targetRole": "CEO|CTO|CMO|etc",
                  "timing": "immediate|this_week|this_month|wait",
                  "painPoints": ["pain1", "pain2", "pain3"],
                  "valueProposition": "Specific value proposition",
                  "approachRationale": "Explanation of chosen strategy"
                }
                """,
                company.getName(),
                company.getIndustry(),
                company.getSize() != null ? company.getSize().name() : "Unknown",
                company.getLocation(),
                company.getAiAnalysis() != null ? company.getAiAnalysis() : "N/A",
                company.getProspectingScore().getValue(),
                icp.getInterestTheme(),
                String.join(", ", icp.getTargetRoles()));
        
        return aiProvider.generateStrategy(prompt, StrategyRecommendation.class);
    }
}
