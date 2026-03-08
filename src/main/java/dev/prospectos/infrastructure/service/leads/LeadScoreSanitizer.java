package dev.prospectos.infrastructure.service.leads;

import dev.prospectos.api.dto.ScoreDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class LeadScoreSanitizer {

    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 100;
    private static final String DEFAULT_PRIORITY = "COLD";

    ScoreDTO sanitize(ScoreDTO score) {
        if (score == null) {
            return new ScoreDTO(MIN_SCORE, DEFAULT_PRIORITY, "No score provided");
        }
        int boundedScore = clamp(score.value());
        String priority = normalizePriority(score.category());
        String reasoning = score.reasoning() != null && !score.reasoning().isBlank()
            ? score.reasoning()
            : "Accepted from lead preview";
        return new ScoreDTO(boundedScore, priority, reasoning);
    }

    private int clamp(int score) {
        if (score < MIN_SCORE) {
            return MIN_SCORE;
        }
        if (score > MAX_SCORE) {
            return MAX_SCORE;
        }
        return score;
    }

    private String normalizePriority(String category) {
        if (category == null || category.isBlank()) {
            return DEFAULT_PRIORITY;
        }
        String normalized = category.trim().toUpperCase();
        return switch (normalized) {
            case "HOT", "WARM", "COLD", "IGNORE" -> normalized;
            default -> {
                log.warn("Unknown priority category '{}', defaulting to {}", category, DEFAULT_PRIORITY);
                yield DEFAULT_PRIORITY;
            }
        };
    }
}
