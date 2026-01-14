package dev.prospectos.ai.example;

import dev.prospectos.ai.client.NewsResponse;
import dev.prospectos.ai.client.ScraperClientInterface;
import dev.prospectos.ai.client.ScrapingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Example demonstrating the new AI-powered scraper client.
 * Run this example to see the scraper in action.
 */
@Slf4j
@Component
@ConditionalOnProperty(value = "scraper.example.enabled", havingValue = "true")
public class ScraperExample implements CommandLineRunner {

    private final ScraperClientInterface scraperClient;

    public ScraperExample(ScraperClientInterface scraperClient) {
        this.scraperClient = scraperClient;
    }

    @Override
    public void run(String... args) {
        log.info("Running Scraper Example...");

        // Example 1: Scrape a company website
        exampleWebsiteScraping();

        // Example 2: Search for company news
        exampleNewsSearch();

        log.info("Scraper Example completed.");
    }

    private void exampleWebsiteScraping() {
        log.info("=== Website Scraping Example ===");

        String website = "https://spring.io";
        boolean deepSearch = false;

        log.info("Scraping website: {} (deep: {})", website, deepSearch);

        ScrapingResponse response = scraperClient.scrapeWebsiteSync(website, deepSearch);

        if (response.success()) {
            log.info("✅ Scraping successful!");
            log.info("📊 Extracted data keys: {}", response.data().keySet());

            // Log some key information
            Object companyName = response.data().get("company_name");
            Object description = response.data().get("description");
            Object emails = response.data().get("emails");

            if (companyName != null) {
                log.info("🏢 Company: {}", companyName);
            }
            if (description != null) {
                log.info("📝 Description: {}", description.toString().substring(0, Math.min(100, description.toString().length())) + "...");
            }
            if (emails != null) {
                log.info("📧 Emails: {}", emails);
            }
        } else {
            log.error("❌ Scraping failed: {}", response.error());
        }
    }

    private void exampleNewsSearch() {
        log.info("=== News Search Example ===");

        String companyName = "Spring";
        int daysBack = 30;

        log.info("Searching news for company: {} (last {} days)", companyName, daysBack);

        NewsResponse response = scraperClient.searchNews(companyName, daysBack);

        log.info("📰 Found {} news items:", response.news().size());
        for (int i = 0; i < Math.min(3, response.news().size()); i++) {
            log.info("  {}. {}", i + 1, response.news().get(i));
        }

        if (response.news().size() > 3) {
            log.info("  ... and {} more items", response.news().size() - 3);
        }
    }
}
