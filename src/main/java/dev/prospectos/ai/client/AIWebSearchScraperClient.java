package dev.prospectos.ai.client;

import dev.prospectos.ai.config.ScraperProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

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
    private final ScraperProperties scraperProperties;
    private final LlmScrapingResponseConverter responseConverter;
    private final ExecutorService executorService;

    public AIWebSearchScraperClient(
        ChatClient chatClient,
        ScraperProperties scraperProperties,
        LlmScrapingResponseConverter responseConverter
    ) {
        this.chatClient = chatClient;
        this.scraperProperties = scraperProperties;
        this.responseConverter = responseConverter;
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public ScrapingResponse scrapeWebsiteSync(String website, boolean deep) {
        int maxRetries = scraperProperties.ai().maxRetries();
        Exception lastException = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                if (attempt > 0) {
                    log.info("Retrying AI web search for website: {} (attempt {}/{})", website, attempt, maxRetries);
                    // Exponential backoff: wait before retry
                    Thread.sleep(1000L * attempt);
                } else {
                    log.info("AI web search scraping website: {} (deep: {})", website, deep);
                }

                // Use deep search from configuration or parameter override
                boolean useDeepSearch = deep || scraperProperties.ai().deepSearchEnabled();
                String prompt = getPrompt(website, useDeepSearch);

                // Execute with timeout protection
                String response = executeWithTimeout(() ->
                    chatClient.prompt(prompt).call().content(),
                    scraperProperties.ai().timeout()
                );

                Map<String, Object> extractedData = responseConverter.convert(response);

                log.debug("AI extracted data for {}: {}", website, extractedData.keySet());
                return new ScrapingResponse(true, extractedData, null);

            } catch (Exception e) {
                lastException = e;
                if (attempt < maxRetries) {
                    log.warn("AI web search failed for website: {} (attempt {}/{}). Error: {}",
                        website, attempt + 1, maxRetries + 1, e.getMessage());
                }
            }
        }

        log.error("AI web search failed for website: {} after {} attempts", website, maxRetries + 1, lastException);
        return new ScrapingResponse(false, null, "AI web search failed after " + (maxRetries + 1) + " attempts: " +
            (lastException != null ? lastException.getMessage() : "Unknown error"));
    }

    private static String getPrompt(String website, boolean useDeepSearch) {
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
            Return ONLY valid JSON.
            Do not use markdown code fences.
            Do not include references, citations, comments, or explanatory text outside JSON.
            """.formatted(website, searchDepth);
        return prompt;
    }

    @Override
    public NewsResponse searchNews(String companyName, int daysBack) {
        int maxRetries = scraperProperties.ai().maxRetries();
        Exception lastException = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                if (attempt > 0) {
                    log.info("Retrying AI news search for company: {} (attempt {}/{})", companyName, attempt, maxRetries);
                    Thread.sleep(1000L * attempt);
                } else {
                    log.info("AI searching news for company: {} (last {} days)", companyName, daysBack);
                }

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

                String response = executeWithTimeout(() ->
                    chatClient.prompt(prompt).call().content(),
                    scraperProperties.ai().timeout()
                );

                List<String> newsItems = parseNewsItems(response);

                log.debug("AI found {} news items for {}", newsItems.size(), companyName);
                return new NewsResponse(newsItems);

            } catch (Exception e) {
                lastException = e;
                if (attempt < maxRetries) {
                    log.warn("AI news search failed for company: {} (attempt {}/{}). Error: {}",
                        companyName, attempt + 1, maxRetries + 1, e.getMessage());
                }
            }
        }

        log.error("AI news search failed for company: {} after {} attempts", companyName, maxRetries + 1, lastException);
        return new NewsResponse(List.of("Error searching news after " + (maxRetries + 1) + " attempts: " +
            (lastException != null ? lastException.getMessage() : "Unknown error")));
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

    /**
     * Executes a task with timeout protection using ExecutorService.
     * If the task exceeds the timeout, it throws a TimeoutException.
     *
     * @param task the task to execute
     * @param timeout the maximum time to wait
     * @return the result of the task
     * @throws TimeoutException if the task times out
     * @throws ExecutionException if the task throws an exception
     * @throws InterruptedException if the thread is interrupted
     */
    private <T> T executeWithTimeout(Supplier<T> task, java.time.Duration timeout)
        throws TimeoutException, ExecutionException, InterruptedException {

        Future<T> future = executorService.submit(task::get);

        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            log.warn("AI request timed out after {}ms", timeout.toMillis());
            throw e;
        }
    }
}
