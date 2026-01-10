package dev.prospectos.ai.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.prospectos.ai.config.ScraperProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * AI-powered scraper client that uses web search capabilities of LLMs
 * instead of traditional web scraping. This approach is more robust,
 * respects website policies, and provides higher quality structured data.
 */
@Slf4j
@Component
@Profile("!mock & !test")
@ConditionalOnProperty(value = "scraper.ai.enabled", havingValue = "true", matchIfMissing = true)
public class AIWebSearchScraperClient implements ScraperClientInterface {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final ScraperProperties scraperProperties;

    public AIWebSearchScraperClient(ChatClient chatClient, ScraperProperties scraperProperties) {
        this.chatClient = chatClient;
        this.objectMapper = new ObjectMapper();
        this.scraperProperties = scraperProperties;
    }

    @Override
    public ScrapingResponse scrapeWebsiteSync(String website, boolean deep) {
        try {
            log.info("AI web search scraping website: {} (deep: {})", website, deep);

            // Use deep search from configuration or parameter override
            boolean useDeepSearch = deep || scraperProperties.ai().deepSearchEnabled();
            String searchDepth = useDeepSearch ? "comprehensive" : "focused";

            String prompt = """
                Research the company at %s and extract the following information:

                1. Company name and business description
                2. Contact information (emails, phone numbers)
                3. Technology stack or tools they use
                4. Industry and company size indicators
                5. Recent developments or news

                Perform a %s search focusing on official company information.

                Return the data in the following JSON format:
                {
                  "company_name": "string",
                  "description": "string",
                  "emails": ["email1", "email2"],
                  "phone": "string",
                  "technologies": ["tech1", "tech2"],
                  "industry": "string",
                  "size": "string",
                  "recent_news": ["news1", "news2"]
                }

                If you cannot find specific information, use null for that field.
                Only include verified information from reliable sources.
                """.formatted(website, searchDepth);

            String response = chatClient.prompt(prompt)
                .call()
                .content();

            Map<String, Object> extractedData = parseAIResponse(response);

            log.debug("AI extracted data for {}: {}", website, extractedData.keySet());
            return new ScrapingResponse(true, extractedData, null);

        } catch (Exception e) {
            log.error("Error during AI web search for website: {}", website, e);
            return new ScrapingResponse(false, null, "AI web search failed: " + e.getMessage());
        }
    }

    @Override
    public NewsResponse searchNews(String companyName, int daysBack) {
        try {
            log.info("AI searching news for company: {} (last {} days)", companyName, daysBack);

            String prompt = """
                Search for recent news and developments about "%s" from the last %d days.

                Focus on business-relevant information such as:
                - Funding rounds or investments
                - Product launches or updates
                - Strategic partnerships
                - Hiring announcements or expansion
                - Market developments
                - Awards or recognitions

                Return 3-5 most relevant and recent news items as a simple list.
                Each item should be a brief, factual summary (1-2 sentences).
                Only include verified information from reliable news sources.

                Format as a simple list of news items, one per line.
                """.formatted(companyName, daysBack);

            String response = chatClient.prompt(prompt)
                .call()
                .content();

            List<String> newsItems = parseNewsItems(response);

            log.debug("AI found {} news items for {}", newsItems.size(), companyName);
            return new NewsResponse(newsItems);

        } catch (Exception e) {
            log.error("Error during AI news search for company: {}", companyName, e);
            return new NewsResponse(List.of("Error searching news: " + e.getMessage()));
        }
    }

    /**
     * Parses AI response to extract structured company data.
     */
    private Map<String, Object> parseAIResponse(String response) {
        try {
            // Try to extract JSON from the response
            String jsonPart = extractJsonFromResponse(response);

            if (jsonPart != null) {
                return objectMapper.readValue(jsonPart, new TypeReference<Map<String, Object>>() {});
            }

            // Fallback: create basic structure from text response
            return Map.of(
                "description", response.trim(),
                "ai_processed", true,
                "source", "ai_web_search"
            );

        } catch (JsonProcessingException e) {
            log.warn("Failed to parse AI response as JSON, using fallback: {}", e.getMessage());
            return Map.of(
                "description", response.trim(),
                "ai_processed", true,
                "parse_error", e.getMessage()
            );
        }
    }

    /**
     * Extracts JSON content from AI response text.
     */
    private String extractJsonFromResponse(String response) {
        if (response == null) return null;

        // Look for JSON block in response
        int jsonStart = response.indexOf('{');
        int jsonEnd = response.lastIndexOf('}');

        if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            return response.substring(jsonStart, jsonEnd + 1);
        }

        return null;
    }

    /**
     * Parses news items from AI response.
     */
    private List<String> parseNewsItems(String response) {
        if (response == null || response.trim().isEmpty()) {
            return List.of();
        }

        // Split by lines and clean up
        return Arrays.stream(response.split("\n"))
            .map(String::trim)
            .filter(line -> !line.isEmpty())
            .filter(line -> !line.startsWith("Here") && !line.startsWith("I found"))
            .map(line -> line.replaceAll("^[-•*]\\s*", "")) // Remove bullet points
            .filter(line -> line.length() > 10) // Filter very short lines
            .limit(10) // Limit to reasonable number
            .toList();
    }
}