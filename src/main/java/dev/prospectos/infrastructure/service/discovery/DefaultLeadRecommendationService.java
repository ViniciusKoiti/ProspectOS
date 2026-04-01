package dev.prospectos.infrastructure.service.discovery;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import dev.prospectos.api.LeadRecommendationService;
import dev.prospectos.api.dto.LeadRecommendationRequest;
import dev.prospectos.api.dto.LeadRecommendationResponse;
import dev.prospectos.api.mcp.QueryMetricsService;
import dev.prospectos.api.mcp.QueryMetricsSnapshot;
import dev.prospectos.api.mcp.QueryTimeWindow;
import dev.prospectos.infrastructure.service.compliance.AllowedSourcesComplianceService;
import org.springframework.stereotype.Service;

@Service
public class DefaultLeadRecommendationService implements LeadRecommendationService {

    private final QueryMetricsService queryMetricsService;
    private final AllowedSourcesComplianceService complianceService;
    private final RecommendationPriority recommendationPriority = new RecommendationPriority();

    public DefaultLeadRecommendationService(QueryMetricsService queryMetricsService, AllowedSourcesComplianceService complianceService) {
        this.queryMetricsService = queryMetricsService;
        this.complianceService = complianceService;
    }

    @Override
    public LeadRecommendationResponse recommend(LeadRecommendationRequest request) {
        var timeWindow = request.timeWindow() == null ? QueryTimeWindow.TWENTY_FOUR_HOURS : QueryTimeWindow.fromValue(request.timeWindow());
        var validated = complianceService.validateSources(request.sources());
        var snapshot = queryMetricsService.getMetrics(timeWindow, null);
        var ranked = rank(validated, snapshot);
        var recommended = ranked.getFirst();
        var fallbacks = ranked.stream().filter(source -> !source.equals(recommended)).toList();
        return new LeadRecommendationResponse(recommended, fallbacks, reasonFor(recommended, snapshot), expectedCost(recommended, snapshot), expectedLatency(recommended, snapshot), timeWindow.value());
    }

    private List<String> rank(List<String> sources, QueryMetricsSnapshot snapshot) {
        var comparator = Comparator.comparingDouble((String source) -> -score(source, snapshot));
        var production = sources.stream().filter(recommendationPriority::isProductionSource).sorted(comparator).toList();
        var support = sources.stream().filter(source -> !recommendationPriority.isProductionSource(source)).sorted(comparator).toList();
        return production.isEmpty() ? support : concat(production, support);
    }

    private List<String> concat(List<String> first, List<String> second) {
        return java.util.stream.Stream.concat(first.stream(), second.stream()).toList();
    }

    private double score(String source, QueryMetricsSnapshot snapshot) {
        return snapshot.providerBreakdown().stream().filter(metric -> metric.provider().equals(normalize(source))).findFirst()
            .map(metric -> metric.successRate() * 1000.0d - metric.avgResponseTime() - metric.cost().doubleValue() * 10.0d + recommendationPriority.priorityOf(source))
            .orElseGet(() -> recommendationPriority.priorityOf(source));
    }

    private String reasonFor(String source, QueryMetricsSnapshot snapshot) {
        String fallbackReason = recommendationPriority.isProductionSource(source)
            ? "Selected %s as the highest-priority configured production source with no observed history yet."
            : "Selected %s because it is the strongest configured source available for this request.";
        return snapshot.providerBreakdown().stream().filter(metric -> metric.provider().equals(normalize(source))).findFirst()
            .map(metric -> "Selected %s based on observed success rate %s and avg response time %dms over 24h.".formatted(source, percent(metric.successRate()), metric.avgResponseTime()))
            .orElse(fallbackReason.formatted(source));
    }

    private BigDecimal expectedCost(String source, QueryMetricsSnapshot snapshot) {
        return snapshot.providerBreakdown().stream().filter(metric -> metric.provider().equals(normalize(source))).findFirst()
            .map(QueryMetricsSnapshot.ProviderMetric::cost).map(cost -> cost.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)).orElse(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }

    private long expectedLatency(String source, QueryMetricsSnapshot snapshot) {
        return snapshot.providerBreakdown().stream().filter(metric -> metric.provider().equals(normalize(source))).findFirst()
            .map(QueryMetricsSnapshot.ProviderMetric::avgResponseTime).orElse(0L);
    }

    private String normalize(String source) {
        return source == null ? "" : source.trim().toLowerCase(Locale.ROOT);
    }

    private String percent(double successRate) {
        return BigDecimal.valueOf(successRate * 100.0d).setScale(0, RoundingMode.HALF_UP) + "%";
    }
}
