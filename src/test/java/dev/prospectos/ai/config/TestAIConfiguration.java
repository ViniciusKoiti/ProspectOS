package dev.prospectos.ai.config;

import dev.prospectos.ai.service.ScoringAIService;
import dev.prospectos.ai.service.OutreachAIService;
import dev.prospectos.ai.service.ProspectorAIService;
import dev.prospectos.ai.service.StrategyAIService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

/**
 * Test configuration that provides mock AI services when real ones are disabled.
 */
@Configuration
@Profile("test")
public class TestAIConfiguration {
    
    @Bean
    public ScoringAIService scoringAIService() {
        return mock(ScoringAIService.class);
    }
    
    @Bean
    public OutreachAIService outreachAIService() {
        return mock(OutreachAIService.class);
    }
    
    @Bean
    public ProspectorAIService prospectorAIService() {
        return mock(ProspectorAIService.class);
    }
    
    @Bean
    public StrategyAIService strategyAIService() {
        return mock(StrategyAIService.class);
    }
}