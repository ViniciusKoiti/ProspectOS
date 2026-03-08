package dev.prospectos.ai.client;

import java.util.Locale;

import dev.prospectos.ai.dto.PriorityLevel;

final class LlmScoringValueParser {

    PriorityLevel parsePriority(Object raw) {
        if (raw == null) {
            return PriorityLevel.COLD;
        }
        try {
            return PriorityLevel.valueOf(String.valueOf(raw).trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return PriorityLevel.COLD;
        }
    }

    String normalizeText(Object raw, String fallback) {
        String value = raw == null ? "" : String.valueOf(raw);
        value = value.replace('\r', ' ').replace('\n', ' ').replace('\t', ' ').trim();
        return value.isBlank() ? fallback : value;
    }

    int parseInt(Object value, int fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
