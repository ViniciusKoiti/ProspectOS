package dev.prospectos.ai.factory;

import dev.prospectos.ai.client.AIProvider;
import dev.prospectos.ai.client.LLMClient;
import dev.prospectos.ai.client.LLMProvider;
import dev.prospectos.ai.client.impl.DefaultAIProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Factory for creating AIProviders.
 * Coordinates different LLM clients for different purposes.
 */
@Slf4j
@Component
public class AIProviderFactory {
    
    private final LLMClientFactory llmClientFactory;
    
    public AIProviderFactory(LLMClientFactory llmClientFactory) {
        this.llmClientFactory = llmClientFactory;
    }
    
    /**
     * Creates the primary AIProvider using the best available client.
     */
    public AIProvider createPrimaryProvider() {
        LLMClient primaryClient = llmClientFactory.createBestAvailableClient();
        LLMClient scoringClient = llmClientFactory.createScoringClient();
        
        log.info("Creating primary AIProvider: {} + {}", 
            primaryClient.getProvider().getDisplayName(),
            scoringClient.getProvider().getDisplayName());
        
        return new DefaultAIProvider(primaryClient, scoringClient);
    }
    
    /**
     * Creates an AIProvider for a specific provider.
     */
    public AIProvider createProvider(LLMProvider provider) {
        LLMClient client = llmClientFactory.createClient(provider);
        
        log.info("Creating AIProvider for: {}", provider.getDisplayName());
        
        return new DefaultAIProvider(client);
    }
    
    /**
     * Creates an AIProvider optimized for scoring.
     */
    public AIProvider createScoringProvider() {
        LLMClient scoringClient = llmClientFactory.createScoringClient();
        
        log.info("Creating AIProvider for scoring: {}", scoringClient.getProvider().getDisplayName());
        
        return new DefaultAIProvider(scoringClient);
    }
    
    /**
     * Creates a mock AIProvider for tests.
     */
    public AIProvider createMockProvider() {
        LLMClient mockClient = llmClientFactory.createClient(LLMProvider.MOCK);
        
        log.info("Creating mock AIProvider for tests");
        
        return new DefaultAIProvider(mockClient);
    }
}
