package dev.prospectos.ai.config;

import dev.prospectos.ai.service.AIPromptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static dev.prospectos.ai.config.AIConfigurationProperties.*;

/**
 * Configuration for AI ChatClients.
 * Responsible ONLY for creating and configuring ChatClients with appropriate prompts.
 */
@Configuration
@ConditionalOnProperty(
    name = AI_ENABLED,
    havingValue = "true",
    matchIfMissing = true
)
@Slf4j
public class AIChatClientConfig {

    private final AIPromptService promptService;

    public AIChatClientConfig(AIPromptService promptService) {
        this.promptService = promptService;
    }

    /**
     * Primary ChatClient with default B2B prospecting prompt.
     */
    @Bean
    @ConditionalOnBean(name = "primaryChatModel")
    public ChatClient chatClient(@Qualifier("primaryChatModel") ChatModel chatModel) {
        log.info("Creating default ChatClient (primary ChatModel present)");
        
        String systemPrompt = promptService.getB2BProspectingPrompt();
        return ChatClient.builder(chatModel)
            .defaultSystem(systemPrompt)
            .build();
    }

    /**
     * Specialized ChatClient for scoring with scoring prompt.
     */
    @Bean("scoringChatClient")
    @ConditionalOnBean(name = "primaryChatModel")
    public ChatClient scoringChatClient(@Qualifier("primaryChatModel") ChatModel chatModel) {
        log.info("Creating scoring ChatClient (primary ChatModel present)");
        
        String systemPrompt = promptService.getScoringPrompt();
        return ChatClient.builder(chatModel)
            .defaultSystem(systemPrompt)
            .build();
    }

    /**
     * Groq-specific ChatClient with default prompt.
     */
    @Bean("groqChatClient")
    @ConditionalOnBean(name = "groqChatModel")
    public ChatClient groqChatClient(@Qualifier("groqChatModel") ChatModel chatModel) {
        log.info("Creating Groq ChatClient");
        
        String systemPrompt = promptService.getB2BProspectingPrompt();
        return ChatClient.builder(chatModel)
            .defaultSystem(systemPrompt)
            .build();
    }

    /**
     * Groq-specific scoring ChatClient.
     */
    @Bean("groqScoringChatClient")
    @ConditionalOnBean(name = "groqChatModel")
    public ChatClient groqScoringChatClient(@Qualifier("groqChatModel") ChatModel chatModel) {
        log.info("Creating Groq scoring ChatClient");
        
        String systemPrompt = promptService.getScoringPrompt();
        return ChatClient.builder(chatModel)
            .defaultSystem(systemPrompt)
            .build();
    }
}