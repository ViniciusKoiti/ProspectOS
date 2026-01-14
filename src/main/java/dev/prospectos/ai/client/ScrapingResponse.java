package dev.prospectos.ai.client;

import java.util.Map;

/**
 * Response from website scraping operation.
 */
public record ScrapingResponse(
    boolean success,
    Map<String, Object> data,
    String error
) {
}
