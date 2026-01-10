package dev.prospectos.ai.client.impl;

import dev.prospectos.ai.client.LLMClient;
import dev.prospectos.ai.client.LLMProvider;
import dev.prospectos.ai.client.mock.MockResponseFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;

/**
 * LLMClient implementation using Spring AI ChatClient.
 * Abstracts Spring AI complexity behind a simple interface.
 */
@Slf4j
public class SpringAILLMClient implements LLMClient {
    
    private final ChatClient chatClient;
    private final LLMProvider provider;
    private final boolean available;
    
    public SpringAILLMClient(ChatClient chatClient, LLMProvider provider, boolean available) {
        this.chatClient = chatClient;
        this.provider = provider;
        this.available = available;
    }
    
    @Override
    public String query(String prompt) {
        if (!available) {
            return "Mock response: " + prompt.substring(0, Math.min(50, prompt.length()));
        }
        
        try {
            log.debug("Executing query on {}: {}", provider.getDisplayName(), 
                prompt.substring(0, Math.min(100, prompt.length())) + "...");
            
            return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
                
        } catch (Exception e) {
            log.error("LLM query error: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
    
    @Override
    public String queryWithFunctions(String prompt, String... functions) {
        if (!available) {
            return "Mock response with functions: " + String.join(", ", functions);
        }
        
        try {
            log.debug("Executing query with functions on {}: {}", provider.getDisplayName(), 
                String.join(", ", functions));
            
            return chatClient.prompt()
                .user(prompt)
                .functions(functions)
                .call()
                .content();
                
        } catch (Exception e) {
            log.error("LLM query with functions error: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
    
    @Override
    public <T> T queryStructured(String prompt, Class<T> responseClass) {
        if (!available) {
            return MockResponseFactory.createMockResponse(responseClass, provider.getDisplayName());
        }
        
        try {
            log.debug("Executing structured query on {}: {}", provider.getDisplayName(), 
                responseClass.getSimpleName());
            
            return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(responseClass);
                
        } catch (Exception e) {
            log.error("Structured LLM query error: {}", e.getMessage());
            throw new RuntimeException("Structured query error", e);
        }
    }
    
    @Override
    public LLMProvider getProvider() {
        return provider;
    }
    
    @Override
    public boolean isAvailable() {
        return available;
    }
}
