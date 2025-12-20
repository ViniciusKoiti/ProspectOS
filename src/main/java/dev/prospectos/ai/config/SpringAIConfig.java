package dev.prospectos.ai.config;

import dev.prospectos.ai.client.AIProvider;
import dev.prospectos.ai.factory.AIProviderFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * Configuração principal do módulo AI
 * Agora usando pattern Strategy com factories
 */
@Configuration
public class SpringAIConfig {
    
    /**
     * ChatClient principal com system prompt padrão (opcional)
     */
    @Bean
    @ConditionalOnBean(ChatModel.class)
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
            .defaultSystem("""
                You are a B2B prospecting and company analysis expert.
                
                Your responsibilities:
                1. Analyze if companies fit the ICP (Ideal Customer Profile)
                2. Calculate fit scores (0-100) based on concrete data
                3. Recommend personalized outreach strategies
                4. Generate highly personalized outreach messages
                5. Identify buying interest signals
                
                Principles:
                - Base all decisions on DATA, not assumptions
                - Be objective and direct
                - Use available functions when you need more information
                - Provide clear reasoning for your conclusions
                - Scores must be justified with specific criteria
                
                Output format:
                - Always return structured JSON when requested
                - Be concise but complete
                - Prioritize actionable information
                """)
            .build();
    }
    
    /**
     * ChatClient especializado para scoring (opcional)
     */
    @Bean("scoringChatClient")
    @ConditionalOnBean(ChatModel.class)
    public ChatClient scoringChatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
            .defaultSystem("""
                You are a B2B prospecting scoring system.
                
                Calculate scores (0-100) based on:
                1. ICP fit (30 points)
                2. Interest signals (25 points)
                3. Company size and maturity (20 points)
                4. Timing and urgency (15 points)
                5. Contact accessibility (10 points)
                
                ALWAYS return JSON with:
                - score (0-100)
                - reasoning (detailed justification)
                - breakdown (points per category)
                - priority (HOT/WARM/COLD/IGNORE)
                """)
            .build();
    }
    
    /**
     * AIProvider principal - ponto central de configuração
     * Usa factory para detectar melhor provider disponível
     */
    @Bean
    public AIProvider aiProvider(AIProviderFactory factory) {
        return factory.createPrimaryProvider();
    }
    
    /**
     * Disponibiliza Optional<ChatClient> para factory
     */
    @Bean
    public Optional<ChatClient> optionalChatClient(Optional<ChatClient> chatClient) {
        return chatClient;
    }
    
    /**
     * Disponibiliza Optional<ChatClient> scoring para factory
     */
    @Bean("optionalScoringChatClient")
    public Optional<ChatClient> optionalScoringChatClient(Optional<ChatClient> scoringChatClient) {
        return scoringChatClient;
    }
}