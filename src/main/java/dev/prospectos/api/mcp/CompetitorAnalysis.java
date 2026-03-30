package dev.prospectos.api.mcp;

import java.util.List;
import java.util.Objects;

/**
 * Individual competitor analysis.
 */
public record CompetitorAnalysis(
    String name,
    double marketShare,
    List<String> targetSegments,
    List<String> strengths,
    List<String> gaps
) {

    public CompetitorAnalysis {
        Objects.requireNonNull(name, "name must not be null");
        targetSegments = List.copyOf(Objects.requireNonNull(targetSegments, "targetSegments must not be null"));
        strengths = List.copyOf(Objects.requireNonNull(strengths, "strengths must not be null"));
        gaps = List.copyOf(Objects.requireNonNull(gaps, "gaps must not be null"));
    }
}
