package dev.prospectos.ai.client;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.prospectos.ai.dto.PriorityLevel;
import dev.prospectos.ai.dto.ScoringResult;

final class LlmScoringTextFallbackParser {

    private static final Pattern SCORE_PATTERN = Pattern.compile("\"score\"\\s*:\\s*(\\d+)");
    private static final Pattern PRIORITY_PATTERN = Pattern.compile("\"priority\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern REASONING_PATTERN = Pattern.compile("\"reasoning\"\\s*:\\s*\"([^\"]+)\"", Pattern.DOTALL);
    private static final Pattern RECOMMENDATION_PATTERN =
        Pattern.compile("\"recommendation\"\\s*:\\s*\"([^\"]+)\"", Pattern.DOTALL);

    private final LlmScoringValueParser valueParser;
    private final LlmScoringBreakdownNormalizer breakdownNormalizer;

    LlmScoringTextFallbackParser(
        LlmScoringValueParser valueParser,
        LlmScoringBreakdownNormalizer breakdownNormalizer
    ) {
        this.valueParser = valueParser;
        this.breakdownNormalizer = breakdownNormalizer;
    }

    ScoringResult parse(String text) {
        int score = valueParser.clamp(extractInt(text, SCORE_PATTERN), 0, 100);
        PriorityLevel priority = valueParser.parsePriority(extractText(text, PRIORITY_PATTERN));
        String reasoning = valueParser.normalizeText(extractText(text, REASONING_PATTERN), "Unable to parse AI reasoning");
        String recommendation = valueParser.normalizeText(
            extractText(text, RECOMMENDATION_PATTERN),
            "No recommendation available."
        );
        Map<String, Integer> breakdown = breakdownNormalizer.defaultBreakdown();
        return new ScoringResult(score, priority, reasoning, breakdown, recommendation);
    }

    int extractScore(String text) {
        return extractInt(text, SCORE_PATTERN);
    }

    private int extractInt(String text, Pattern pattern) {
        if (text == null) {
            return 0;
        }
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return 0;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String extractText(String text, Pattern pattern) {
        if (text == null) {
            return null;
        }
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
