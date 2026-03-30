package dev.prospectos.api.mcp;

import java.util.List;
import java.util.Objects;

/**
 * Search quality assessment.
 */
public record SearchQualityMetrics(
    double overallScore,
    double dataCompleteness,
    double accuracy,
    double freshness,
    int duplicatesFound,
    List<String> qualityIssues
) {

    public SearchQualityMetrics {
        qualityIssues = List.copyOf(Objects.requireNonNull(qualityIssues, "qualityIssues must not be null"));
    }
}
