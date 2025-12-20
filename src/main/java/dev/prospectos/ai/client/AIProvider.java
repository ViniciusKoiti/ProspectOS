package dev.prospectos.ai.client;

/**
 * Interface para diferentes estratégias de AI 
 * Permite implementações especializadas para diferentes tipos de análise
 */
public interface AIProvider {
    
    /**
     * Análise de adequação empresa-ICP
     * @param prompt dados da empresa e ICP
     * @return decisão se deve investigar (true/false)
     */
    boolean analyzeICPFit(String prompt);
    
    /**
     * Enriquecimento de dados da empresa usando function calling
     * @param prompt dados da empresa
     * @return análise enriquecida
     */
    String enrichCompanyData(String prompt);
    
    /**
     * Cálculo de score estruturado
     * @param prompt dados para scoring
     * @param responseClass classe do resultado
     * @return objeto de scoring
     */
    <T> T calculateScore(String prompt, Class<T> responseClass);
    
    /**
     * Geração de estratégia de abordagem
     * @param prompt dados da empresa e contexto
     * @param responseClass classe da estratégia
     * @return estratégia estruturada
     */
    <T> T generateStrategy(String prompt, Class<T> responseClass);
    
    /**
     * Geração de mensagem de outreach
     * @param prompt dados para personalização
     * @param responseClass classe da mensagem
     * @return mensagem estruturada
     */
    <T> T generateOutreach(String prompt, Class<T> responseClass);
    
    /**
     * Retorna o cliente LLM subjacente
     * @return cliente LLM
     */
    LLMClient getClient();
    
    /**
     * Verifica se o provider está disponível
     * @return true se disponível
     */
    boolean isAvailable();
}