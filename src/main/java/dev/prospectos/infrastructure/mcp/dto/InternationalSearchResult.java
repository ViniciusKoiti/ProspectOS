package dev.prospectos.infrastructure.mcp.dto;

import dev.prospectos.api.mcp.InternationalSearchOutcome;

import java.math.BigDecimal;
import java.util.List;

public record InternationalSearchResult(
    List<LeadResult> leads,
    BigDecimal totalCost,
    double avgQualityScore,
    List<String> optimizationHints
) {

    public static InternationalSearchResult fromDomain(InternationalSearchOutcome outcome) {
        var leads = outcome.leads().stream()
            .map(lead -> new LeadResult(
                lead.name(),
                lead.country(),
                lead.sourceProvider(),
                lead.qualityScore()
            ))
            .toList();

        return new InternationalSearchResult(
            leads,
            outcome.totalCost(),
            outcome.avgQualityScore(),
            outcome.optimizationHints()
        );
    }

    public record LeadResult(String name, String country, String sourceProvider, double qualityScore) {
    }
}
