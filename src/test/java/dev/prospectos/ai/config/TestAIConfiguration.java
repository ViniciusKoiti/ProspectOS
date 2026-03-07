package dev.prospectos.ai.config;

import dev.prospectos.ai.dto.OutreachMessage;
import dev.prospectos.ai.dto.StrategyRecommendation;
import dev.prospectos.ai.service.ScoringAIService;
import dev.prospectos.core.domain.Company;
import dev.prospectos.ai.service.OutreachAIService;
import dev.prospectos.ai.service.ProspectorAIService;
import dev.prospectos.ai.service.StrategyAIService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test configuration that provides functional mock AI services when real ones are disabled.
 */
@Configuration
@Profile("test")
@ConditionalOnProperty(
    name = "prospectos.scoring.mock.enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class TestAIConfiguration {
    
    @Bean
    public ScoringAIService scoringAIService() {
        ScoringAIService mock = mock(ScoringAIService.class);
        when(mock.scoreCompany(any(), any())).thenAnswer(invocation -> {
            Company company = invocation.getArgument(0);
            // Return different scores based on company name for test scenarios
            String companyName = company.getName() != null ? company.getName().toLowerCase() : "";
            
            if (companyName.contains("stone") || companyName.contains("rd station")) { // HIGH_POTENTIAL_COMPANY_ID (2L)
                    return new dev.prospectos.ai.dto.ScoringResult(
                        85,
                        dev.prospectos.ai.dto.PriorityLevel.HOT,
                        "High potential company with strong ICP match",
                        Map.of("icpFit", 30, "signals", 25, "companySize", 20, "timing", 15, "accessibility", 5),
                        "Prioritize immediate outreach"
                    );
                } else if (companyName.contains("pagseguro") || companyName.contains("creditas")) { // LOW_POTENTIAL_COMPANY_ID (3L)
                    return new dev.prospectos.ai.dto.ScoringResult(
                        35,
                        dev.prospectos.ai.dto.PriorityLevel.COLD,
                        "Low potential company with weak ICP match",
                        Map.of("icpFit", 10, "signals", 8, "companySize", 7, "timing", 5, "accessibility", 5),
                        "Add to nurture campaign"
                    );
                }
            
            // Default score for other companies (low potential)
            return new dev.prospectos.ai.dto.ScoringResult(
                35,
                dev.prospectos.ai.dto.PriorityLevel.COLD,
                "Low potential company for testing purposes",
                Map.of("icpFit", 10, "signals", 8, "companySize", 7, "timing", 5, "accessibility", 5),
                "Add to nurture campaign"
            );
        });
        return mock;
    }
    
    @Bean
    public OutreachAIService outreachAIService() {
        OutreachAIService mock = mock(OutreachAIService.class);
        when(mock.generateOutreach(any(), any())).thenReturn(
            new OutreachMessage(
                "Partnership Opportunity - AI Solutions",
                "Hi there! I noticed your company's innovative approach to technology...",
                "email",
                "professional",
                new String[]{"Schedule a call", "Learn more"}
            )
        );
        return mock;
    }
    
    @Bean
    public ProspectorAIService prospectorAIService() {
        ProspectorAIService mock = mock(ProspectorAIService.class);
        when(mock.enrichCompany(any())).thenReturn("AI-generated company analysis with market insights and competitive positioning");
        when(mock.shouldInvestigateCompany(any(), any())).thenReturn(true);
        return mock;
    }
    
    @Bean
    public StrategyAIService strategyAIService() {
        StrategyAIService mock = mock(StrategyAIService.class);
        when(mock.recommendStrategy(any(), any())).thenReturn(
            new StrategyRecommendation(
                "email",
                "CTO",
                "within 1 week",
                List.of("Technical debt", "Scalability challenges", "AI adoption"),
                "Our AI solutions can reduce technical debt by 40%",
                "Direct technical approach focusing on concrete ROI"
            )
        );
        return mock;
    }
}