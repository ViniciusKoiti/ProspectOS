package dev.prospectos.core.enrichment;

import java.util.Arrays;

final class EnrichmentTextNormalizer {

    String normalizeCompanyName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        String normalized = name.trim().replaceAll("\\s+", " ");
        normalized = normalized.replaceAll("\\s+(Inc\\.?|LLC|Ltd\\.?|Corporation|Corp\\.?)\\s*$", "");
        return capitalizeWords(normalized);
    }

    String normalizeDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return null;
        }
        String normalized = description.trim().replaceAll("\\s+", " ");
        if (normalized.length() > 500) {
            normalized = normalized.substring(0, 497) + "...";
        }
        return normalized.isEmpty() ? null : normalized;
    }

    String normalizePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        String normalized = phone.replaceAll("[^\\d+\\-\\(\\)\\s.]", "");
        normalized = normalized.trim().replaceAll("\\s+", " ");
        return normalized.matches(".*\\d.*") && normalized.length() >= 7 ? normalized : null;
    }

    String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return Arrays.stream(text.split("\\s+"))
            .map(word -> word.isEmpty()
                ? word
                : Character.toUpperCase(word.charAt(0)) + (word.length() > 1 ? word.substring(1).toLowerCase() : ""))
            .reduce((a, b) -> a + " " + b)
            .orElse("");
    }
}
