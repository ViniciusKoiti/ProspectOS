package dev.prospectos.ai.client;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Stub client for scraper integration.
 * This will be replaced when the scraper module is implemented.
 */
@Component
public class ScraperClient {
    
    public ScrapingResponse scrapeWebsiteSync(String website, boolean deep) {
        // Stub implementation
        return new ScrapingResponse(
            true, 
            Map.of(
                "company_name", "Example Company",
                "description", "A sample company for testing",
                "technologies", List.of("Java", "Spring", "React"),
                "emails", List.of("contact@example.com"),
                "phone", "+1-555-0123"
            ),
            null
        );
    }
    
    public NewsResponse searchNews(String companyName, int daysBack) {
        // Stub implementation  
        return new NewsResponse(
            List.of(
                "Company " + companyName + " announces new product launch",
                "Company " + companyName + " raises $10M in Series A funding",
                "Company " + companyName + " expands to new markets"
            )
        );
    }
    
    public record ScrapingResponse(
        boolean success,
        Map<String, Object> data,
        String error
    ) {}
    
    public record NewsResponse(
        List<String> news
    ) {}
}