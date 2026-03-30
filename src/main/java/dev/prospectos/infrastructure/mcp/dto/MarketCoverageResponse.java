package dev.prospectos.infrastructure.mcp.dto;

import dev.prospectos.api.mcp.*;
import java.util.List;
import java.util.Map;

public record MarketCoverageResponse(
    String country,
    double totalMarketSize,
    double coveredMarketShare,
    List<String> uncoveredSegments,
    Map<String, CompetitorAnalysisResponse> competitorAnalysis,
    List<String> opportunities
) {

    public static MarketCoverageResponse fromDomain(MarketCoverageAnalysis analysis) {
        var competitors = analysis.competitorAnalysis().entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                entry -> CompetitorAnalysisResponse.fromDomain(entry.getValue())
            ));

        return new MarketCoverageResponse(
            analysis.country(),
            analysis.totalMarketSize(),
            analysis.coveredMarketShare(),
            analysis.uncoveredSegments(),
            competitors,
            analysis.opportunities()
        );
    }

    public record CompetitorAnalysisResponse(
        String name,
        double marketShare,
        List<String> targetSegments,
        List<String> strengths,
        List<String> gaps
    ) {
        public static CompetitorAnalysisResponse fromDomain(CompetitorAnalysis analysis) {
            return new CompetitorAnalysisResponse(
                analysis.name(),
                analysis.marketShare(),
                analysis.targetSegments(),
                analysis.strengths(),
                analysis.gaps()
            );
        }
    }
}
