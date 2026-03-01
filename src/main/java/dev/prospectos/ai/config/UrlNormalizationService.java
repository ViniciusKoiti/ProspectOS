package dev.prospectos.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static dev.prospectos.ai.config.AIConfigurationProperties.DEFAULT_GROQ_BASE_URL;

/**
 * Service for normalizing AI provider URLs.
 * Centralizes URL handling logic to eliminate duplication.
 */
@Component
@Slf4j
public class UrlNormalizationService {

    /**
     * Normalizes Groq base URL to ensure proper format.
     * 
     * @param baseUrl the base URL to normalize (can be null or empty)
     * @return normalized URL ending with /v1
     */
    public String normalizeGroqUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            log.debug("Using default Groq URL: {}", DEFAULT_GROQ_BASE_URL);
            return DEFAULT_GROQ_BASE_URL + "/v1";
        }

        String normalized = baseUrl.trim();
        
        // Split URL and query parameters
        String[] parts = normalized.split("\\?", 2);
        String urlPart = parts[0];
        String queryPart = parts.length > 1 ? "?" + parts[1] : "";
        
        // Remove trailing slashes from URL part
        while (urlPart.endsWith("/")) {
            urlPart = urlPart.substring(0, urlPart.length() - 1);
        }
        
        // Ensure v1 endpoint in URL part
        if (!urlPart.endsWith("/v1")) {
            urlPart = urlPart + "/v1";
        }
        
        // Reconstruct full URL with query parameters
        normalized = urlPart + queryPart;
        
        log.debug("Normalized Groq URL: {} -> {}", baseUrl, normalized);
        return normalized;
    }

    /**
     * Validates that a URL is properly formatted.
     * 
     * @param url the URL to validate
     * @return true if URL is valid
     */
    public boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        try {
            return url.startsWith("http://") || url.startsWith("https://");
        } catch (Exception e) {
            log.warn("Invalid URL format: {}", url);
            return false;
        }
    }
}