package dev.prospectos.core.enrichment;

import java.util.List;

/**
 * Raw data from scraper that needs normalization and validation.
 * This represents the unprocessed input that will be cleaned and validated.
 */
public record EnrichmentRequest(
    String companyName,
    String description,
    List<String> emails,        // Raw email strings (may be invalid)
    String phone,
    List<String> technologies,
    String industry,
    String size,
    List<String> recentNews,
    String website
) {
    public EnrichmentRequest {
        // Null-safe initialization for lists
        if (emails == null) {
            emails = List.of();
        }
        if (technologies == null) {
            technologies = List.of();
        }
        if (recentNews == null) {
            recentNews = List.of();
        }
    }
}