package dev.prospectos.ai.service;

import dev.prospectos.ai.client.AIProvider;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Main AI service for prospecting decisions.
 * Uses interfaces to abstract LLM providers.
 */
@Slf4j
@Service
public class ProspectorAIService {
    
    private final AIProvider aiProvider;
    
    public ProspectorAIService(AIProvider aiProvider) {
        this.aiProvider = aiProvider;
    }
    
    /**
     * AI decides whether a company is worth investigating.
     */
    public boolean shouldInvestigateCompany(Company company, ICP icp) {
        log.info("AI analyzing whether to investigate: {}", company.getName());
        
        String prompt = String.format("""
                Company: %s
                Website: %s
                Industry: %s
                Location: %s
                
                ICP (Ideal Profile):
                - Target Industries: %s
                - Target Regions: %s
                - Interest Theme: %s
                
                Decision: Is this company worth investigating further? 
                Answer only: YES or NO
                """,
                company.getName(),
                company.getWebsite().getUrl(),
                company.getIndustry(),
                company.getLocation(),
                String.join(", ", icp.getIndustries()),
                String.join(", ", icp.getRegions()),
                icp.getInterestTheme());
        
        boolean should = aiProvider.analyzeICPFit(prompt);
        
        log.info("   Decision: {} - {}", 
            should ? "INVESTIGATE" : "SKIP",
            company.getName()
        );
        
        return should;
    }
    
    /**
     * AI analyzes and enriches company data using function calling.
     */
    public String enrichCompanyWithAI(Company company) {
        log.info("AI enriching company: {}", company.getName());
        
        String prompt = String.format("""
                Analyze this company and enrich with relevant information for B2B prospecting.
                
                Company: %s
                Website: %s
                
                Provide a strategic analysis focused on:
                1. Potential fit with our ICP
                2. Growth or change signals
                3. Likely pain points
                4. Best recommended approach
                
                Be specific and actionable. Maximum 200 words.
                """,
                company.getName(),
                company.getWebsite().getUrl());
        
        return aiProvider.enrichCompanyData(prompt);
    }
    
    /**
     * AI recommends an outreach strategy.
     */
    public String recommendApproachStrategy(Company company, ICP icp) {
        log.info("AI recommending strategy: {}", company.getName());
        
        String prompt = String.format("""
                Company: %s
                AI analysis: %s
                Score: %s
                
                ICP:
                - Theme: %s
                - Target roles: %s
                
                Based on this information, recommend:
                1. Best outreach channel (email, LinkedIn, phone)
                2. Best person/role to contact
                3. Best timing
                4. Key pain points to mention
                5. Specific value proposition
                
                Be specific and actionable.
                """,
                company.getName(),
                company.getAiAnalysis() != null ? company.getAiAnalysis() : "Not available",
                company.getProspectingScore().getValue(),
                icp.getInterestTheme(),
                String.join(", ", icp.getTargetRoles()));
        
        return aiProvider.getClient().queryWithFunctions(prompt, "analyzeCompanySignals");
    }
}
