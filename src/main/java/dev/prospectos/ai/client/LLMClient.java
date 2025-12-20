package dev.prospectos.ai.client;

/**
 * Interface abstrata para clientes LLM
 * Permite trocar entre diferentes provedores (OpenAI, Claude, Ollama) transparentemente
 */
public interface LLMClient {
    
    /**
     * Executa uma consulta simples ao LLM
     * @param prompt texto do prompt
     * @return resposta do LLM
     */
    String query(String prompt);
    
    /**
     * Executa uma consulta com function calling habilitado
     * @param prompt texto do prompt  
     * @param functions nomes das funções que o LLM pode chamar
     * @return resposta do LLM
     */
    String queryWithFunctions(String prompt, String... functions);
    
    /**
     * Executa uma consulta retornando objeto estruturado
     * @param prompt texto do prompt
     * @param responseClass classe do objeto de resposta
     * @return objeto parseado automaticamente
     */
    <T> T queryStructured(String prompt, Class<T> responseClass);
    
    /**
     * Retorna o tipo do provedor LLM
     * @return tipo do provedor
     */
    LLMProvider getProvider();
    
    /**
     * Verifica se o cliente está disponível (API key configurada, etc)
     * @return true se disponível
     */
    boolean isAvailable();
}