package dev.prospectos.ai.client;

/**
 * Interface for scraper client implementations.
 * Supports both traditional scraping and AI-powered web search approaches.
 */
public interface ScraperClientInterface {

    /**
     * Scrapes a website to extract company information.
     *
     * @param website URL of the website to scrape
     * @param deep whether to perform deep scraping (multiple pages)
     * @return scraping response with extracted data
     */
    ScrapingResponse scrapeWebsiteSync(String website, boolean deep);

    /**
     * Searches for recent news about a company.
     *
     * @param companyName name of the company to search for
     * @param daysBack number of days to look back
     * @return news response with recent articles
     */
    NewsResponse searchNews(String companyName, int daysBack);
}
