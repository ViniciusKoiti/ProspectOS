package dev.prospectos.ai.mock;

import dev.prospectos.ai.dto.OutreachMessage;
import dev.prospectos.ai.dto.PriorityLevel;
import dev.prospectos.ai.dto.ScoringResult;
import dev.prospectos.ai.dto.StrategyRecommendation;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Central factory for typed mock structured responses.
 */
@Slf4j
public final class MockStructuredResponseFactory {

    private MockStructuredResponseFactory() {
    }

    public static <T> T create(Class<T> responseClass, String providerName) {
        Supplier<?> supplier = mockSuppliers(providerName).get(responseClass);
        if (supplier == null) {
            log.warn("Cannot create mock for {}, returning null", responseClass.getSimpleName());
            return null;
        }
        return responseClass.cast(supplier.get());
    }

    private static Map<Class<?>, Supplier<?>> mockSuppliers(String providerName) {
        String resolvedProvider = providerName == null ? "Mock" : providerName;

        return Map.of(
            ScoringResult.class,
            () -> new ScoringResult(
                85,
                PriorityLevel.HOT,
                "Mock scoring result from " + resolvedProvider + " provider. High fit based on technology stack and growth indicators.",
                Map.of(
                    "icpFit", 28,
                    "signals", 22,
                    "companySize", 18,
                    "timing", 12,
                    "accessibility", 5
                ),
                "Prioritize immediate outreach. Company shows strong alignment with our ICP."
            ),
            StrategyRecommendation.class,
            () -> new StrategyRecommendation(
                "email",
                "CTO",
                "immediate",
                List.of("Legacy infrastructure", "Scaling challenges", "Security concerns"),
                "Mock value proposition from " + resolvedProvider,
                "Mock approach rationale - target technical decision makers with infrastructure focus"
            ),
            OutreachMessage.class,
            () -> new OutreachMessage(
                "Performance optimization for [COMPANY]",
                "Mock outreach message from " + resolvedProvider + " provider.",
                "email",
                "consultative",
                new String[]{"Schedule demo", "Download case study"}
            )
        );
    }
}
