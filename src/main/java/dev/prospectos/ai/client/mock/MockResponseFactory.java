package dev.prospectos.ai.client.mock;

import dev.prospectos.ai.dto.PriorityLevel;
import dev.prospectos.ai.dto.ScoringResult;
import dev.prospectos.ai.dto.StrategyRecommendation;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Factory dedicated to creating AI response mocks.
 * Centralizes all logic for structured mock creation.
 */
@Slf4j
public class MockResponseFactory {
    
    /**
     * Creates a mock response based on the requested type.
     */
    @SuppressWarnings("unchecked")
    public static <T> T createMockResponse(Class<T> responseClass, String providerName) {
        log.debug("Creating mock for {} from {}", responseClass.getSimpleName(), providerName);
        
        return switch (responseClass.getSimpleName()) {
            case "ScoringResult" -> (T) createMockScoringResult(providerName);
            case "StrategyRecommendation" -> (T) createMockStrategyRecommendation(providerName);
            default -> createGenericMock(responseClass);
        };
    }
    
    private static ScoringResult createMockScoringResult(String providerName) {
        return new ScoringResult(
            85,
            PriorityLevel.HOT,
            "Mock scoring result from " + providerName + " provider. High fit based on technology stack and growth indicators.",
            Map.of(
                "icpFit", 28,
                "signals", 22,
                "companySize", 18,
                "timing", 12,
                "accessibility", 5
            ),
            "Prioritize immediate outreach. Company shows strong alignment with our ICP."
        );
    }
    
    private static StrategyRecommendation createMockStrategyRecommendation(String providerName) {
        return new StrategyRecommendation(
            "email",
            "CTO",
            "immediate",
            List.of("Legacy infrastructure", "Scaling challenges", "Security concerns"),
            "Mock value proposition from " + providerName,
            "Mock approach rationale - target technical decision makers with infrastructure focus"
        );
    }
    
    @SuppressWarnings("unchecked")
    private static <T> T createGenericMock(Class<T> responseClass) {
        try {
            return responseClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.warn("Cannot create mock for {}, returning null", responseClass.getSimpleName());
            return null;
        }
    }
}
