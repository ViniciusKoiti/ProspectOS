package dev.prospectos.ai.client.impl;

import dev.prospectos.ai.client.AIProvider;
import dev.prospectos.ai.client.LLMClient;
import lombok.extern.slf4j.Slf4j;

/**
 * Default AIProvider implementation.
 * Centralizes interaction logic with LLMs for different analysis types.
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
        log.info("Analyzing ICP fit via {}", llmClient.getProvider().getDisplayName());
        
        String enhancedPrompt = prompt + "\n\nRespond ONLY with 'YES' or 'NO'.";
        String response = llmClient.query(enhancedPrompt);
        
        boolean fits = response.trim().toUpperCase().startsWith("YES");
        log.info("   Result: {} via {}", fits ? "FIT" : "NOT_FIT", 
            llmClient.getProvider().getDisplayName());
        
        return fits;
    }
    
    @Override
    public String enrichCompanyData(String prompt) {
        log.info("Enriching company data via {}", functionLLMClient.getProvider().getDisplayName());
        
        return functionLLMClient.queryWithFunctions(
            prompt, 
            "scrapeWebsite", 
            "searchCompanyNews"
        );
    }
    
    @Override
    public <T> T calculateScore(String prompt, Class<T> responseClass) {
        log.info("Calculating score via {}", llmClient.getProvider().getDisplayName());
        
        return llmClient.queryStructured(prompt, responseClass);
    }
    
    @Override
    public <T> T generateStrategy(String prompt, Class<T> responseClass) {
        log.info("Generating strategy via {}", functionLLMClient.getProvider().getDisplayName());
        
        return functionLLMClient.queryStructured(prompt, responseClass);
    }
    
    @Override
    public <T> T generateOutreach(String prompt, Class<T> responseClass) {
        log.info("Generating outreach via {}", llmClient.getProvider().getDisplayName());
        
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
