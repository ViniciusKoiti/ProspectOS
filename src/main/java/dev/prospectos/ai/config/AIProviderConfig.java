package dev.prospectos.ai.config;

import dev.prospectos.ai.client.AIProvider;
import dev.prospectos.ai.factory.AIProviderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static dev.prospectos.ai.config.AIConfigurationProperties.*;

/**
 * Configuration for AI Providers.
 * Responsible ONLY for creating and configuring the primary AIProvider.
 */
@Configuration
@ConditionalOnProperty(
    name = AI_ENABLED,
    havingValue = "true",
    matchIfMissing = true
)
@Slf4j
public class AIProviderConfig {

    /**
     * Primary AIProvider - configuration entry point.
     * Uses the factory to detect the best available provider.
     */
    @Bean
    public AIProvider aiProvider(AIProviderFactory factory) {
        log.info("Creating primary AIProvider using factory");
        
        AIProvider provider = factory.createPrimaryProvider();
        log.info("✅ Primary AIProvider created: {}", provider.getClass().getSimpleName());
        
        return provider;
    }
}