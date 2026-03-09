package dev.prospectos.ai.client;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

final class LlmScoringBreakdownNormalizer {

    private final LlmScoringValueParser valueParser;

    LlmScoringBreakdownNormalizer(LlmScoringValueParser valueParser) {
        this.valueParser = valueParser;
    }

    Map<String, Integer> normalize(Object rawBreakdown) {
        Map<String, Integer> normalized = defaultBreakdown();
        if (!(rawBreakdown instanceof Map<?, ?> breakdownMap)) {
            return normalized;
        }
        for (Map.Entry<?, ?> entry : breakdownMap.entrySet()) {
            String key = canonicalBreakdownKey(String.valueOf(entry.getKey()));
            if (key == null) {
                continue;
            }
            int max = maxForKey(key);
            int value = valueParser.clamp(valueParser.parseInt(entry.getValue(), 0), 0, max);
            normalized.put(key, value);
        }
        return normalized;
    }

    Map<String, Integer> defaultBreakdown() {
        Map<String, Integer> normalized = new LinkedHashMap<>();
        normalized.put("icpFit", 0);
        normalized.put("signals", 0);
        normalized.put("companySize", 0);
        normalized.put("timing", 0);
        normalized.put("accessibility", 0);
        return normalized;
    }

    private String canonicalBreakdownKey(String key) {
        if (key == null) {
            return null;
        }
        String normalized = key.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        return switch (normalized) {
            case "icpfit" -> "icpFit";
            case "interestsignals", "signals" -> "signals";
            case "companysizeandmaturity", "companysize" -> "companySize";
            case "timingandurgency", "timing" -> "timing";
            case "contactaccessibility", "accessibility" -> "accessibility";
            default -> null;
        };
    }

    private int maxForKey(String key) {
        return switch (key) {
            case "icpFit" -> 30;
            case "signals" -> 25;
            case "companySize" -> 20;
            case "timing" -> 15;
            case "accessibility" -> 10;
            default -> 100;
        };
    }
}
