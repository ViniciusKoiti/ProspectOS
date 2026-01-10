package dev.prospectos.ai.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for scraper functionality.
 * Enables ScraperProperties and provides any additional scraper-related beans.
 */
@Configuration
@EnableConfigurationProperties(ScraperProperties.class)
public class ScraperConfiguration {
    // Additional scraper configuration beans can be added here if needed
}