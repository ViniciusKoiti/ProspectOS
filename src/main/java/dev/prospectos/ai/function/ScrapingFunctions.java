package dev.prospectos.ai.function;

import dev.prospectos.ai.client.ScraperClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.Map;
import java.util.function.Function;

/**
 * Functions that LLMs can call to perform scraping.
 */
@Slf4j
@Configuration
public class ScrapingFunctions {
    
    private final ScraperClient scraperClient;
    
    public ScrapingFunctions(ScraperClient scraperClient) {
        this.scraperClient = scraperClient;
    }
    
    /**
     * LLM can call this function to scrape a website.
     */
    @Bean
    @Description("""
        Scrapes a corporate website to extract data.
        Use when you need information about a company that was not provided.
        
        Returns: emails, phone numbers, tech stack, about the company, etc.
        """)
    public Function<ScrapingRequest, Map<String, Object>> scrapeWebsite() {
        return request -> {
            log.info("LLM called scrapeWebsite: {}", request.website());
            
            ScraperClient.ScrapingResponse response = scraperClient.scrapeWebsiteSync(
                request.website(),
                request.deep()
            );
            
            if (response.success()) {
                return response.data();
            } else {
                return Map.of(
                    "error", response.error(),
                    "success", false
                );
            }
        };
    }
    
    /**
     * Request DTO for scraping.
     */
    public record ScrapingRequest(
        @Description("Website URL to analyze") 
        String website,
        
        @Description("If true, performs deep scraping (multiple pages)") 
        boolean deep
    ) {}
}
