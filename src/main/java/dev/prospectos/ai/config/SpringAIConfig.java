package dev.prospectos.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import static dev.prospectos.ai.config.AIConfigurationProperties.*;

/**
 * Main configuration for the AI module.
 * 
 * This configuration has been refactored to separate concerns:
 * - AIChatClientConfig: Handles ChatClient creation and configuration
 * - AIProviderConfig: Handles AIProvider creation
 * - AIPromptService: Manages system prompts from external files
 * 
 * This class now serves mainly as a coordination point and can be used
 * for any cross-cutting AI configuration concerns.
 */
@Configuration
@ConditionalOnProperty(
    name = AI_ENABLED,
    havingValue = "true",
    matchIfMissing = true
)
@Slf4j
public class SpringAIConfig {

    public SpringAIConfig() {
        log.info("✅ AI module configuration initialized - responsibilities properly separated");
    }
}
