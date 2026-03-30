package dev.prospectos.infrastructure.mcp.dto;

import dev.prospectos.api.mcp.*;

import java.util.List;
import java.util.Map;

public record MarketAnalysisResponse(
    String country,
    String industry,
    Map<String, Object> marketMetrics,
    List<String> insights,
    Map<String, CompetitorResponse> competitors
) {

    public static MarketAnalysisResponse fromDomain(MarketAnalysisData data) {
        var competitors = data.competitors().entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                entry -> CompetitorResponse.fromDomain(entry.getValue())
            ));

        return new MarketAnalysisResponse(
            data.country(),
            data.industry(),
            data.marketMetrics(),
            data.insights(),
            competitors
        );
    }

    public record CompetitorResponse(
        String name,
        double marketShare,
        List<String> strengths,
        List<String> weaknesses
    ) {
        public static CompetitorResponse fromDomain(CompetitorData competitor) {
            return new CompetitorResponse(
                competitor.name(),
                competitor.marketShare(),
                competitor.strengths(),
                competitor.weaknesses()
            );
        }
    }
}
