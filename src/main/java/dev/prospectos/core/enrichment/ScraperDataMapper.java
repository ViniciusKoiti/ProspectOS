package dev.prospectos.core.enrichment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utility class to convert scraper response data to EnrichmentRequest.
 */
public class ScraperDataMapper {

    /**
     * Converts scraper response data to EnrichmentRequest.
     */
    public static EnrichmentRequest fromScraperData(Map<String, Object> scraperData, String websiteUrl) {
        if (scraperData == null) {
            throw new IllegalArgumentException("Scraper data cannot be null");
        }

        return new EnrichmentRequest(
            extractString(scraperData, "company_name"),
            extractString(scraperData, "description"),
            extractStringList(scraperData, "emails"),
            extractString(scraperData, "phone"),
            extractStringList(scraperData, "technologies"),
            extractString(scraperData, "industry"),
            extractString(scraperData, "size"),
            extractStringList(scraperData, "recent_news"),
            websiteUrl // Use the original website URL passed to scraper
        );
    }

    /**
     * Safely extracts a string value from the map.
     */
    private static String extractString(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        return value.toString().trim();
    }

    /**
     * Safely extracts a list of strings from the map.
     */
    @SuppressWarnings("unchecked")
    private static List<String> extractStringList(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            return Collections.emptyList();
        }

        if (value instanceof List) {
            List<?> list = (List<?>) value;
            List<String> result = new ArrayList<>();
            for (Object item : list) {
                if (item != null) {
                    result.add(item.toString().trim());
                }
            }
            return result;
        }

        // If it's a single string, wrap it in a list
        return List.of(value.toString().trim());
    }
}