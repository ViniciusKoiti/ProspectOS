package dev.prospectos.ai.service;

import dev.prospectos.ai.client.AIProvider;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Serviço principal de IA para decisões de prospecção
 * Agora usa interfaces para abstrair provedores LLM
 */
@Slf4j
@Service
public class ProspectorAIService {
    
    private final AIProvider aiProvider;
    
    public ProspectorAIService(AIProvider aiProvider) {
        this.aiProvider = aiProvider;
    }
    
    /**
     * AI decide se vale a pena investigar uma empresa
     */
    public boolean shouldInvestigateCompany(Company company, ICP icp) {
        log.info("🤖 AI analyzing if should investigate: {}", company.getName());
        
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
            should ? "✅ INVESTIGATE" : "❌ SKIP",
            company.getName()
        );
        
        return should;
    }
    
    /**
     * AI analisa e enriquece dados da empresa usando function calling
     */
    public String enrichCompanyWithAI(Company company) {
        log.info("🤖 AI enriching company: {}", company.getName());
        
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
     * AI sugere estratégia de abordagem
     */
    public String recommendApproachStrategy(Company company, ICP icp) {
        log.info("🤖 AI recommending strategy: {}", company.getName());
        
        String prompt = String.format("""
                Empresa: %s
                Análise AI: %s
                Score: %s
                
                ICP:
                - Tema: %s
                - Cargos alvo: %s
                
                Com base nestas informações, recomende:
                1. Melhor canal de abordagem (email, LinkedIn, phone)
                2. Melhor pessoa/cargo para contatar
                3. Melhor momento (timing)
                4. Principais pain points a mencionar
                5. Proposta de valor específica
                
                Seja específico e acionável.
                """,
                company.getName(),
                company.getAiAnalysis() != null ? company.getAiAnalysis() : "Not available",
                company.getProspectingScore().getValue(),
                icp.getInterestTheme(),
                String.join(", ", icp.getTargetRoles()));
        
        return aiProvider.getClient().queryWithFunctions(prompt, "analyzeCompanySignals");
    }
}