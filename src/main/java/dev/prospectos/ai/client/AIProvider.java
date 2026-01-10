package dev.prospectos.ai.client;

/**
 * Interface for different AI strategies.
 * Allows specialized implementations for different analysis types.
 */
public interface AIProvider {
    
    /**
     * Company-to-ICP fit analysis.
     * @param prompt company and ICP data
     * @return decision on whether to investigate (true/false)
     */
    boolean analyzeICPFit(String prompt);
    
    /**
     * Company data enrichment using function calling.
     * @param prompt company data
     * @return enriched analysis
     */
    String enrichCompanyData(String prompt);
    
    /**
     * Structured score calculation.
     * @param prompt scoring data
     * @param responseClass result class
     * @return scoring object
     */
    <T> T calculateScore(String prompt, Class<T> responseClass);
    
    /**
     * Outreach strategy generation.
     * @param prompt company data and context
     * @param responseClass strategy class
     * @return structured strategy
     */
    <T> T generateStrategy(String prompt, Class<T> responseClass);
    
    /**
     * Outreach message generation.
     * @param prompt personalization data
     * @param responseClass message class
     * @return structured message
     */
    <T> T generateOutreach(String prompt, Class<T> responseClass);
    
    /**
     * Returns the underlying LLM client.
     * @return LLM client
     */
    LLMClient getClient();
    
    /**
     * Checks whether the provider is available.
     * @return true if available
     */
    boolean isAvailable();
}
