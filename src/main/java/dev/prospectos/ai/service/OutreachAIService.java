package dev.prospectos.ai.service;

import dev.prospectos.ai.client.AIProvider;
import dev.prospectos.ai.dto.OutreachMessage;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Personalized outreach message generation.
 */
@Slf4j
@Service
public class OutreachAIService {
    
    private final AIProvider aiProvider;
    
    public OutreachAIService(AIProvider aiProvider) {
        this.aiProvider = aiProvider;
    }
    
    /**
     * Generates a personalized outreach message.
     */
    public OutreachMessage generateOutreach(Company company, ICP icp) {
        log.info("AI generating outreach: {}", company.getName());
        
        String prompt = String.format("""
                Create a highly personalized B2B outreach message.
                
                TARGET COMPANY:
                %s - %s
                Analysis: %s
                Recommended Strategy: %s
                
                YOUR PRODUCT/SERVICE:
                Theme: %s
                Target Roles: %s
                
                GUIDELINES:
                1. Start with a personalized hook based on the analysis
                2. Demonstrate you've researched the company
                3. Connect an identified pain point with your solution
                4. Be concise (max 150 words)
                5. Clear and low-commitment CTA
                6. Professional but not overly corporate tone
                
                Return JSON:
                {
                  "subject": "Email subject",
                  "body": "Message body",
                  "channel": "email|linkedin|phone",
                  "tone": "formal|casual|consultative",
                  "callsToAction": ["CTA1", "CTA2"]
                }
                """,
                company.getName(),
                company.getIndustry(),
                company.getAiAnalysis() != null ? company.getAiAnalysis() : "N/A",
                company.getRecommendedApproach() != null ? company.getRecommendedApproach() : "N/A",
                icp.getInterestTheme(),
                String.join(", ", icp.getTargetRoles()));
        
        return aiProvider.generateOutreach(prompt, OutreachMessage.class);
    }
}
