package dev.prospectos.ai.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Mock implementation of ScraperClient for testing and development.
 * Used when AI scraping is disabled or in test environments.
 */
@Component
@Profile({"test", "mock"})
public class MockScraperClient implements ScraperClientInterface {

    @Override
    public ScrapingResponse scrapeWebsiteSync(String website, boolean deep) {
        // Mock implementation with realistic test data
        return new ScrapingResponse(
            true,
            Map.of(
                "company_name", "Example Company",
                "description", "A sample company for testing purposes",
                "technologies", List.of("Java", "Spring", "React", "PostgreSQL"),
                "emails", List.of("contact@example.com", "info@example.com"),
                "phone", "+1-555-0123",
                "industry", "Software Development",
                "size", "50-100 employees",
                "source", "mock_data"
            ),
            null
        );
    }

    @Override
    public NewsResponse searchNews(String companyName, int daysBack) {
        // Mock implementation with realistic test news
        return new NewsResponse(
            List.of(
                "Company " + companyName + " announces new product launch focused on AI integration",
                "Company " + companyName + " raises $10M in Series A funding led by TechVC",
                "Company " + companyName + " expands to European markets with new office in Berlin",
                "Company " + companyName + " partners with major enterprise client for digital transformation",
                "Company " + companyName + " wins 'Best Innovation Award' at TechConf 2025"
            )
        );
    }
}