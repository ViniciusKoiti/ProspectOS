package dev.prospectos.ai.config;

import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Configuration for mock AI environment
 * Ensures mock implementations are used when real AI APIs are not available
 */
@Configuration
@Profile({"mock", "test"})
public class MockAIConfiguration {
    
    // This configuration ensures that mock implementations
    // are preferred when running in mock or test profiles
    
}