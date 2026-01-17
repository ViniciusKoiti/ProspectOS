package dev.prospectos.ai.example;

import dev.prospectos.ai.client.AIProvider;
import dev.prospectos.ai.client.LLMProvider;
import dev.prospectos.ai.dto.OutreachMessage;
import dev.prospectos.ai.dto.ScoringResult;
import dev.prospectos.ai.dto.StrategyRecommendation;
import dev.prospectos.ai.factory.AIProviderFactory;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ICPDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Example usage of the new AI architecture with interfaces.
 * Demonstrates how to switch between LLM providers transparently.
 */
@Slf4j
@Component
public class AIUsageExample {

    private final AIProviderFactory providerFactory;
    public AIUsageExample(AIProviderFactory providerFactory) {
        this.providerFactory = providerFactory;
    }

    /**
     * Example of a full company analysis using multiple providers.
     */
    public void demonstrateFullAnalysis() {
        // Example data using DTOs
        CompanyDTO company = new CompanyDTO(
            1L,
            "TechCorp",
            "Software",
            "https://techcorp.com",
            "Leading tech company",
            150,
            "Sao Paulo"
        );

        ICPDto icp = new ICPDto(
            1L,
            "SaaS B2B",
            "Growing software companies",
            List.of("Software", "Technology"),
            List.of("North America", "South America"),
            List.of("Docker", "Kubernetes", "AWS"),
            50,
            500,
            List.of("CTO", "VP Engineering"),
            "Cloud transformation and DevOps adoption"
        );

        log.info("=== AI ARCHITECTURE DEMONSTRATION ===");

        // 1. Use primary provider (auto-detects the best available)
        demonstratePrimaryProvider(company, icp);

        // 2. Use a specific provider for comparison
        demonstrateSpecificProvider(company, icp, LLMProvider.MOCK);

        // 3. Demonstrate provider switching at runtime
        demonstrateProviderSwitching(company, icp);
    }

    private void demonstratePrimaryProvider(CompanyDTO company, ICPDto icp) {
        log.info("\n1. USING PRIMARY PROVIDER");

        AIProvider primary = providerFactory.createPrimaryProvider();
        log.info("Selected provider: {}", primary.getClient().getProvider().getDisplayName());

        // Full analysis
        boolean shouldInvestigate = analyzeFit(primary, company, icp);
        if (shouldInvestigate) {
            ScoringResult score = calculateScore(primary, company, icp);
            StrategyRecommendation strategy = generateStrategy(primary, company, icp);
            OutreachMessage outreach = generateOutreach(primary, company, icp);
        }
    }

    private void demonstrateSpecificProvider(CompanyDTO company, ICPDto icp, LLMProvider provider) {
        log.info("\n2. USING SPECIFIC PROVIDER: {}", provider.getDisplayName());

        AIProvider specific = providerFactory.createProvider(provider);
        log.info("Configured provider: {}", specific.getClient().getProvider().getDisplayName());

        // Analysis using the specific provider
        boolean shouldInvestigate = analyzeFit(specific, company, icp);
        if (shouldInvestigate) {
            ScoringResult score = calculateScore(specific, company, icp);
        }
    }

    private void demonstrateProviderSwitching(CompanyDTO company, ICPDto icp) {
        log.info("\n3. DEMONSTRATING PROVIDER SWITCHING");

        // Test each available provider
        for (LLMProvider provider : LLMProvider.values()) {
            try {
                AIProvider ai = providerFactory.createProvider(provider);
                if (ai.isAvailable()) {
                    log.info("Testing {}: {}",
                        provider.getDisplayName(),
                        ai.getClient().query("Connectivity test"));
                } else {
                    log.info("{} not available", provider.getDisplayName());
                }
            } catch (Exception e) {
                log.warn("Error for {}: {}", provider.getDisplayName(), e.getMessage());
            }
        }
    }

    private boolean analyzeFit(AIProvider ai, CompanyDTO company, ICPDto icp) {
        String prompt = String.format(
            "Company: %s, Industry: %s. ICP: %s. Fit?",
            company.name(), company.industry(), icp.description()
        );

        boolean fits = ai.analyzeICPFit(prompt);
        log.info("   ICP fit: {}", fits ? "YES" : "NO");
        return fits;
    }

    private ScoringResult calculateScore(AIProvider ai, CompanyDTO company, ICPDto icp) {
        String prompt = String.format(
            "Score company %s from %s industry for ICP %s",
            company.name(), company.industry(), icp.description()
        );

        ScoringResult result = ai.calculateScore(prompt, ScoringResult.class);
        log.info("   Score: {} ({})", result.score(), result.priority());
        return result;
    }

    private StrategyRecommendation generateStrategy(AIProvider ai, CompanyDTO company, ICPDto icp) {
        String prompt = String.format(
            "Strategy for %s from %s industry",
            company.name(), company.industry()
        );

        StrategyRecommendation strategy = ai.generateStrategy(prompt, StrategyRecommendation.class);
        log.info("   Strategy: {} via {}", strategy.channel(), strategy.targetRole());
        return strategy;
    }

    private OutreachMessage generateOutreach(AIProvider ai, CompanyDTO company, ICPDto icp) {
        String prompt = String.format(
            "Outreach for %s about %s",
            company.name(), icp.description()
        );

        OutreachMessage outreach = ai.generateOutreach(prompt, OutreachMessage.class);
        log.info("   Outreach: {}", outreach.subject());
        return outreach;
    }
}
