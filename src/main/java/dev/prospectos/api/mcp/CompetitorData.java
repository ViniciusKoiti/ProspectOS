package dev.prospectos.api.mcp;

import java.util.List;
import java.util.Objects;

/**
 * Competitor analysis data.
 */
public record CompetitorData(
    String name,
    double marketShare,
    List<String> strengths,
    List<String> weaknesses
) {

    public CompetitorData {
        Objects.requireNonNull(name, "name must not be null");
        strengths = List.copyOf(Objects.requireNonNull(strengths, "strengths must not be null"));
        weaknesses = List.copyOf(Objects.requireNonNull(weaknesses, "weaknesses must not be null"));
    }
}
