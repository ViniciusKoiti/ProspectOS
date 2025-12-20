package dev.prospectos.ai.client.impl;

import dev.prospectos.ai.client.AIProvider;
import dev.prospectos.ai.client.LLMClient;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementação padrão do AIProvider
 * Centraliza a lógica de interação com LLMs para diferentes tipos de análise
 */
@Slf4j
public class DefaultAIProvider implements AIProvider {
    
    private final LLMClient llmClient;
    private final LLMClient functionLLMClient;
    
    public DefaultAIProvider(LLMClient llmClient, LLMClient functionLLMClient) {
        this.llmClient = llmClient;
        this.functionLLMClient = functionLLMClient;
    }
    
    public DefaultAIProvider(LLMClient llmClient) {
        this(llmClient, llmClient);
    }
    
    @Override
    public boolean analyzeICPFit(String prompt) {
        log.info("🎯 Analisando adequação ao ICP via {}", llmClient.getProvider().getDisplayName());
        
        String enhancedPrompt = prompt + "\n\nResponda APENAS com 'SIM' ou 'NÃO'.";
        String response = llmClient.query(enhancedPrompt);
        
        boolean fits = response.trim().toUpperCase().startsWith("SIM");
        log.info("   Resultado: {} via {}", fits ? "✅ ADEQUADO" : "❌ NÃO ADEQUADO", 
            llmClient.getProvider().getDisplayName());
        
        return fits;
    }
    
    @Override
    public String enrichCompanyData(String prompt) {
        log.info("🔍 Enriquecendo dados da empresa via {}", functionLLMClient.getProvider().getDisplayName());
        
        return functionLLMClient.queryWithFunctions(
            prompt, 
            "scrapeWebsite", 
            "searchCompanyNews"
        );
    }
    
    @Override
    public <T> T calculateScore(String prompt, Class<T> responseClass) {
        log.info("📊 Calculando score via {}", llmClient.getProvider().getDisplayName());
        
        return llmClient.queryStructured(prompt, responseClass);
    }
    
    @Override
    public <T> T generateStrategy(String prompt, Class<T> responseClass) {
        log.info("🎯 Gerando estratégia via {}", functionLLMClient.getProvider().getDisplayName());
        
        return functionLLMClient.queryStructured(prompt, responseClass);
    }
    
    @Override
    public <T> T generateOutreach(String prompt, Class<T> responseClass) {
        log.info("✉️ Gerando outreach via {}", llmClient.getProvider().getDisplayName());
        
        return llmClient.queryStructured(prompt, responseClass);
    }
    
    @Override
    public LLMClient getClient() {
        return llmClient;
    }
    
    @Override
    public boolean isAvailable() {
        return llmClient.isAvailable() && functionLLMClient.isAvailable();
    }
}