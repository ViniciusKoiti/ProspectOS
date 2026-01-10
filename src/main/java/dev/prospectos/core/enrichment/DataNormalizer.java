package dev.prospectos.core.enrichment;

import dev.prospectos.core.domain.Company.CompanySize;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

/**
 * Handles field cleanup and standardization for enrichment data.
 */
@Component
public class DataNormalizer {

    private static final Map<String, String> INDUSTRY_MAPPINGS = Map.ofEntries(
        Map.entry("tech", "Technology"),
        Map.entry("technology", "Technology"),
        Map.entry("software", "Technology"),
        Map.entry("it", "Technology"),
        Map.entry("finance", "Financial Services"),
        Map.entry("fintech", "Financial Services"),
        Map.entry("healthcare", "Healthcare"),
        Map.entry("health", "Healthcare"),
        Map.entry("retail", "Retail"),
        Map.entry("e-commerce", "Retail"),
        Map.entry("manufacturing", "Manufacturing"),
        Map.entry("education", "Education"),
        Map.entry("consulting", "Consulting")
    );

    private static final Map<String, CompanySize> SIZE_MAPPINGS = Map.of(
        "startup", CompanySize.STARTUP,
        "small", CompanySize.SMALL,
        "medium", CompanySize.MEDIUM,
        "large", CompanySize.LARGE,
        "enterprise", CompanySize.ENTERPRISE,
        "1-10", CompanySize.STARTUP,
        "11-50", CompanySize.SMALL,
        "51-200", CompanySize.MEDIUM,
        "201-1000", CompanySize.LARGE,
        "1000+", CompanySize.ENTERPRISE
    );

    /**
     * Normalizes company name by trimming whitespace and fixing common issues.
     */
    public String normalizeCompanyName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        String normalized = name.trim();

        // Remove multiple consecutive spaces
        normalized = normalized.replaceAll("\\s+", " ");

        // Remove common suffixes like Inc., Ltd., etc. if they appear multiple times
        normalized = normalized.replaceAll("\\s+(Inc\\.?|LLC|Ltd\\.?|Corporation|Corp\\.?)\\s*$", "");

        // Capitalize first letter of each word
        normalized = capitalizeWords(normalized);

        return normalized;
    }

    /**
     * Cleans and normalizes description text.
     */
    public String normalizeDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return null;
        }

        String normalized = description.trim();

        // Remove excessive whitespace
        normalized = normalized.replaceAll("\\s+", " ");

        // Remove leading/trailing punctuation
        normalized = normalized.replaceAll("^[^a-zA-Z0-9]*|[^a-zA-Z0-9]*$", "");

        // Limit length to reasonable size (500 chars)
        if (normalized.length() > 500) {
            normalized = normalized.substring(0, 497) + "...";
        }

        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * Standardizes industry names using common mappings.
     */
    public String standardizeIndustry(String industry) {
        if (industry == null || industry.trim().isEmpty()) {
            return "Other";
        }

        String normalized = industry.trim().toLowerCase();

        // Check for exact mappings first
        String mapped = INDUSTRY_MAPPINGS.get(normalized);
        if (mapped != null) {
            return mapped;
        }

        // Check for partial matches
        for (Map.Entry<String, String> entry : INDUSTRY_MAPPINGS.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Return capitalized version of original
        return capitalizeWords(industry.trim());
    }

    /**
     * Normalizes phone number format.
     */
    public String normalizePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }

        // Remove all non-digits and common separators
        String normalized = phone.replaceAll("[^\\d+\\-\\(\\)\\s.]", "");

        // Remove excessive whitespace
        normalized = normalized.trim().replaceAll("\\s+", " ");

        // If it looks like a phone number (has digits), return it
        if (normalized.matches(".*\\d.*") && normalized.length() >= 7) {
            return normalized;
        }

        return null;
    }

    /**
     * Maps size strings to CompanySize enum.
     */
    public CompanySize mapCompanySize(String sizeString) {
        if (sizeString == null || sizeString.trim().isEmpty()) {
            return null;
        }

        String normalized = sizeString.trim().toLowerCase();

        // Check for exact mappings
        CompanySize mapped = SIZE_MAPPINGS.get(normalized);
        if (mapped != null) {
            return mapped;
        }

        // Try to extract numbers and map based on employee count
        String numbersOnly = normalized.replaceAll("[^\\d\\-+]", "");
        if (numbersOnly.contains("-")) {
            String[] parts = numbersOnly.split("-");
            try {
                int lowerBound = Integer.parseInt(parts[0]);
                if (lowerBound <= 10) return CompanySize.STARTUP;
                if (lowerBound <= 50) return CompanySize.SMALL;
                if (lowerBound <= 200) return CompanySize.MEDIUM;
                if (lowerBound <= 1000) return CompanySize.LARGE;
                return CompanySize.ENTERPRISE;
            } catch (NumberFormatException e) {
                // Fall through to default
            }
        }

        // Check for keywords
        if (normalized.contains("startup") || normalized.contains("small")) {
            return CompanySize.SMALL;
        }
        if (normalized.contains("enterprise") || normalized.contains("large")) {
            return CompanySize.LARGE;
        }
        if (normalized.contains("medium")) {
            return CompanySize.MEDIUM;
        }

        return null; // Unknown size
    }

    /**
     * Capitalizes the first letter of each word.
     */
    private String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        return Arrays.stream(text.split("\\s+"))
            .map(word -> {
                if (word.isEmpty()) return word;
                return Character.toUpperCase(word.charAt(0)) +
                       (word.length() > 1 ? word.substring(1).toLowerCase() : "");
            })
            .reduce((a, b) -> a + " " + b)
            .orElse("");
    }
}
