package dev.prospectos.ai.factory;

import dev.prospectos.ai.client.AIProvider;
import dev.prospectos.ai.client.LLMClient;
import dev.prospectos.ai.client.LLMProvider;
import dev.prospectos.ai.client.impl.DefaultAIProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Factory para criação de AIProviders
 * Coordena diferentes clientes LLM para diferentes propósitos
 */
@Slf4j
@Component
public class AIProviderFactory {
    
    private final LLMClientFactory llmClientFactory;
    
    public AIProviderFactory(LLMClientFactory llmClientFactory) {
        this.llmClientFactory = llmClientFactory;
    }
    
    /**
     * Cria AIProvider principal usando melhor cliente disponível
     */
    public AIProvider createPrimaryProvider() {
        LLMClient primaryClient = llmClientFactory.createBestAvailableClient();
        LLMClient scoringClient = llmClientFactory.createScoringClient();
        
        log.info("🚀 Criando AIProvider principal: {} + {}", 
            primaryClient.getProvider().getDisplayName(),
            scoringClient.getProvider().getDisplayName());
        
        return new DefaultAIProvider(primaryClient, scoringClient);
    }
    
    /**
     * Cria AIProvider para um provedor específico
     */
    public AIProvider createProvider(LLMProvider provider) {
        LLMClient client = llmClientFactory.createClient(provider);
        
        log.info("🚀 Criando AIProvider para: {}", provider.getDisplayName());
        
        return new DefaultAIProvider(client);
    }
    
    /**
     * Cria AIProvider otimizado para scoring
     */
    public AIProvider createScoringProvider() {
        LLMClient scoringClient = llmClientFactory.createScoringClient();
        
        log.info("🚀 Criando AIProvider para scoring: {}", scoringClient.getProvider().getDisplayName());
        
        return new DefaultAIProvider(scoringClient);
    }
    
    /**
     * Cria AIProvider mock para testes
     */
    public AIProvider createMockProvider() {
        LLMClient mockClient = llmClientFactory.createClient(LLMProvider.MOCK);
        
        log.info("🧪 Criando AIProvider mock para testes");
        
        return new DefaultAIProvider(mockClient);
    }
}