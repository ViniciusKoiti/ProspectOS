package dev.prospectos.ai.example;

import dev.prospectos.ai.client.AIProvider;
import dev.prospectos.ai.dto.ScoringResult;
import dev.prospectos.ai.factory.AIProviderFactory;
import dev.prospectos.core.api.CoreDataService;
import dev.prospectos.core.api.dto.CompanyDTO;
import dev.prospectos.core.api.dto.ICPDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Simple AI demonstration that runs automatically when the app starts
 * Works with mock providers - no API keys required
 */
@Component
@Profile({"mock", "demo"})
public class SimpleAIDemo implements CommandLineRunner {

    private final AIProviderFactory providerFactory;
    private final CoreDataService coreDataService;
    
    public SimpleAIDemo(AIProviderFactory providerFactory, CoreDataService coreDataService) {
        this.providerFactory = providerFactory;
        this.coreDataService = coreDataService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 === ProspectOS AI Demo (Using Mocks) ===\n");

        // Create test data using DTOs
        CompanyDTO company = new CompanyDTO(
            1L,
            "TechCorp Solutions",
            "Software",
            "https://techcorp.com",
            "Growing software company",
            150,
            "São Paulo"
        );

        ICPDto icp = new ICPDto(
            1L,
            "SaaS B2B",
            "Growing software companies",
            List.of("Software", "Technology"),
            List.of("Docker", "Kubernetes", "AWS"),
            50,
            500,
            List.of("CTO", "VP Engineering")
        );

        // Get AI provider (will use mock automatically)
        AIProvider provider = providerFactory.createPrimaryProvider();

        System.out.println("📊 Provider: " + provider.getClient().getProvider().getDisplayName());
        System.out.println("🔗 Available: " + provider.isAvailable());
        System.out.println();

        // Test ICP fit analysis
        System.out.println("📋 1. ICP Fit Analysis");
        String fitPrompt = String.format("Company: %s, Industry: %s. ICP: %s. Fit?",
                company.name(), company.industry(), icp.description());

        boolean fits = provider.analyzeICPFit(fitPrompt);
        System.out.println("   Result: " + (fits ? "✅ YES" : "❌ NO"));
        System.out.println();

        // Test scoring
        System.out.println("📊 2. Company Scoring");
        String scorePrompt = String.format("Score company %s from %s industry for ICP %s",
                company.name(), company.industry(), icp.description());

        ScoringResult score = provider.calculateScore(scorePrompt, ScoringResult.class);
        System.out.println("   Score: " + score.score() + "/100");
        System.out.println("   Priority: " + score.priority());
        System.out.println("   Reasoning: " + score.reasoning());
        System.out.println();

        System.out.println("🎉 Demo completed successfully!");
        System.out.println("💡 This used mock responses - no API keys required!");
    }
}