package dev.prospectos.ai.dto;

import java.util.List;

public record StrategyRecommendation(
    String channel,
    String targetRole,
    String timing,
    List<String> painPoints,
    String valueProposition,
    String approachRationale
) {}