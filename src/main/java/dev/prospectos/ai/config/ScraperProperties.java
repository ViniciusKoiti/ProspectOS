package dev.prospectos.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Configuration properties for scraper functionality.
 * Supports both AI-powered web search and traditional scraping configurations.
 */
@ConfigurationProperties(prefix = "scraper")
public record ScraperProperties(

    /**
     * Whether to use AI-powered web search for scraping.
     * When enabled, uses LLM web search capabilities instead of traditional scraping.
     */
    @DefaultValue("true")
    boolean aiEnabled,

    /**
     * AI-specific configuration for web search scraping.
     */
    @DefaultValue
    Ai ai,

    /**
     * Service configuration for external scraper service (legacy/fallback).
     */
    @DefaultValue
    Service service

) {

    /**
     * AI configuration for web search scraping.
     */
    public record Ai(

        /**
         * Maximum timeout for AI web search requests.
         */
        @DefaultValue("30s")
        Duration timeout,

        /**
         * Maximum number of retry attempts for failed AI requests.
         */
        @DefaultValue("2")
        int maxRetries,

        /**
         * Whether to enable deep/comprehensive search mode by default.
         */
        @DefaultValue("false")
        boolean deepSearchEnabled,

        /**
         * Cache duration for AI search results to avoid redundant searches.
         */
        @DefaultValue("1h")
        Duration cacheTimeout

    ) {}

    /**
     * External scraper service configuration (legacy/fallback).
     */
    public record Service(

        /**
         * URL of the external scraper service.
         */
        @DefaultValue("http://localhost:3000")
        String url,

        /**
         * Timeout for external scraper service requests.
         */
        @DefaultValue("30s")
        Duration timeout,

        /**
         * Maximum number of retry attempts for failed service requests.
         */
        @DefaultValue("3")
        int maxRetries

    ) {}
}