package dev.prospectos.ai.client;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import dev.prospectos.ai.config.ScraperProperties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("!test")
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
        return AIWebSearchRetryExecutor.runWithRetries(log, "web search", website, maxRetries, () -> {
            boolean useDeepSearch = deep || scraperProperties.ai().deepSearchEnabled();
            String prompt = AIWebSearchPromptBuilder.scrapePrompt(website, useDeepSearch);
            String response = AIWebSearchTimeoutExecutor.execute(
                () -> chatClient.prompt(prompt).call().content(),
                scraperProperties.ai().timeout(),
                executorService,
                log
            );
            Map<String, Object> extractedData = responseConverter.convert(response);
            log.debug("AI extracted data for {}: {}", website, extractedData.keySet());
            return new ScrapingResponse(true, extractedData, null);
        }, e -> new ScrapingResponse(
            false,
            null,
            "AI web search failed after " + (maxRetries + 1) + " attempts: " + messageOrUnknown(e)
        ));
    }

    @Override
    public NewsResponse searchNews(String companyName, int daysBack) {
        int maxRetries = scraperProperties.ai().maxRetries();
        return AIWebSearchRetryExecutor.runWithRetries(log, "news search", companyName, maxRetries, () -> {
            String prompt = AIWebSearchPromptBuilder.newsPrompt(companyName, daysBack);
            String response = AIWebSearchTimeoutExecutor.execute(
                () -> chatClient.prompt(prompt).call().content(),
                scraperProperties.ai().timeout(),
                executorService,
                log
            );
            List<String> newsItems = AIWebSearchNewsParser.parse(response);
            log.debug("AI found {} news items for {}", newsItems.size(), companyName);
            return new NewsResponse(newsItems);
        }, e -> new NewsResponse(List.of(
            "Error searching news after " + (maxRetries + 1) + " attempts: " + messageOrUnknown(e)
        )));
    }

    private String messageOrUnknown(Exception exception) {
        return exception != null ? exception.getMessage() : "Unknown error";
    }
}
