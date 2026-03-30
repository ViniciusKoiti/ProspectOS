package dev.prospectos.infrastructure.mcp.tools;

import dev.prospectos.api.mcp.LeadData;
import dev.prospectos.api.mcp.LeadSearchCriteria;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
class InternationalSearchMcpInputParser {

    String normalizeRequired(String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(field + " parameter is required");
        }
        return value.trim().toLowerCase();
    }

    String normalizeMarket(String market) {
        return normalizeRequired(market, "Market").replace(" ", "-");
    }

    LeadSearchCriteria buildSearchCriteria(String maxResults, String budgetLimit, String minQualityScore, String requiredFields) {
        return new LeadSearchCriteria(
            parseInteger(maxResults, "maxResults", 20, 1, 100),
            parseDouble(budgetLimit, "budgetLimit", 50.0, 1.0, 10000.0),
            parseDouble(minQualityScore, "minQualityScore", 0.7, 0.0, 1.0),
            parseCsv(requiredFields, List.of("companyName", "website"), false),
            new HashMap<>()
        );
    }

    List<String> parseSources(String sources) {
        return parseCsv(sources, List.of("linkedin", "web-scraping", "google-places"), true);
    }

    List<String> parseCompetitors(String competitors) {
        return parseCsv(competitors, List.of(), false);
    }

    LeadData buildLeadDataForEnrichment(String leadId, String companyName, String website) {
        var normalizedWebsite = StringUtils.hasText(website)
            ? website.trim()
            : "https://www." + companyName.toLowerCase().replace(" ", "") + ".com";
        return new LeadData(leadId, companyName, normalizedWebsite, "unknown", "unknown", "unknown", Map.of(), 0.5);
    }

    double parseRequiredDouble(String value, String paramName) {
        return parseDouble(value, paramName, 0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    private List<String> parseCsv(String value, List<String> defaultValues, boolean normalizeValues) {
        if (!StringUtils.hasText(value)) {
            return defaultValues;
        }
        return Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .map(entry -> normalizeValues ? entry.toLowerCase().replace(" ", "-") : entry)
            .toList();
    }

    private int parseInteger(String value, String paramName, int defaultValue, int min, int max) {
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        try {
            var parsed = Integer.parseInt(value.trim());
            if (parsed < min || parsed > max) {
                throw new IllegalArgumentException(paramName + " must be between " + min + " and " + max + ", got: " + parsed);
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid " + paramName + ": " + value);
        }
    }

    private double parseDouble(String value, String paramName, double defaultValue, double min, double max) {
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        try {
            var parsed = Double.parseDouble(value.trim());
            if (parsed < min || parsed > max) {
                throw new IllegalArgumentException(paramName + " must be between " + min + " and " + max + ", got: " + parsed);
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid " + paramName + ": " + value);
        }
    }
}
