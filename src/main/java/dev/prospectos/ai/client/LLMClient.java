package dev.prospectos.ai.client;

/**
 * Abstract interface for LLM clients.
 * Allows transparent switching between providers (OpenAI, Claude, Ollama).
 */
public interface LLMClient {
    
    /**
     * Executes a simple LLM query.
     * @param prompt prompt text
     * @return LLM response
     */
    String query(String prompt);
    
    /**
     * Executes a query with function calling enabled.
     * @param prompt prompt text
     * @param functions names of functions the LLM can call
     * @return LLM response
     */
    String queryWithFunctions(String prompt, String... functions);
    
    /**
     * Executes a query returning a structured object.
     * @param prompt prompt text
     * @param responseClass response object class
     * @return object parsed automatically
     */
    <T> T queryStructured(String prompt, Class<T> responseClass);
    
    /**
     * Returns the LLM provider type.
     * @return provider type
     */
    LLMProvider getProvider();
    
    /**
     * Checks whether the client is available (API key configured, etc).
     * @return true if available
     */
    boolean isAvailable();
}
