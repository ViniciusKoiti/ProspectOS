package dev.prospectos.ai.client.impl;

import dev.prospectos.ai.client.LLMClient;
import dev.prospectos.ai.client.LLMProvider;
import dev.prospectos.ai.mock.MockStructuredResponseFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * Mock implementation of LLMClient for tests.
 * Simulates realistic responses without external API dependencies.
 */
@Slf4j
public class MockLLMClient implements LLMClient {
    
    private final LLMProvider provider;

    public MockLLMClient() {
        this(LLMProvider.MOCK);
    }

    public MockLLMClient(LLMProvider provider) {
        this.provider = provider;
    }

    @Override
    public String query(String prompt) {
        log.debug("Mock LLM query: {}", prompt.substring(0, Math.min(100, prompt.length())));

        String normalizedPrompt = prompt.toLowerCase();

        if (normalizedPrompt.contains("food") || normalizedPrompt.contains("restaurant")) {
            return "NO";
        }
        
        if (normalizedPrompt.contains("yes or no")) {
            return "YES";
        }
        
        if (normalizedPrompt.contains("company")) {
            return "This is a technology company with great potential for our ICP. " +
                   "Based on website analysis, they use modern technologies like Java and Spring, " +
                   "have a team of 50-200 employees and are in expansion phase.";
        }
        
        if (prompt.toLowerCase().contains("strategy")) {
            return "I recommend LinkedIn approach targeting CTO or VP Engineering. " +
                   "Ideal timing: next 2 weeks. Pain points: scalability and technical modernization. " +
                   "Value proposition: 30% reduction in operational costs.";
        }
        
        return "Mock response for: " + prompt.substring(0, Math.min(50, prompt.length())) + "...";
    }
    
    @Override
    public String queryWithFunctions(String prompt, String... functions) {
        log.debug("Mock LLM query with functions: {}", String.join(", ", functions));
        
        return "Mock response with data collected via functions: " + String.join(", ", functions) + 
               ". Complete company analysis shows strong ICP fit.";
    }
    
    @Override
    public <T> T queryStructured(String prompt, Class<T> responseClass) {
        log.debug("Mock LLM structured query: {}", responseClass.getSimpleName());

        return MockStructuredResponseFactory.create(responseClass, provider.getDisplayName());
    }
    
    @Override
    public LLMProvider getProvider() {
        return provider;
    }
    
    @Override
    public boolean isAvailable() {
        return true;
    }
    
}
