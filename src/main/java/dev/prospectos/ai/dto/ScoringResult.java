package dev.prospectos.ai.dto;

import java.util.Map;

public record ScoringResult(
    int score,
    PriorityLevel priority,
    String reasoning,
    Map<String, Integer> breakdown,
    String recommendation
) {}