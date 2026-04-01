package dev.prospectos.infrastructure.mcp.service;

import dev.prospectos.api.mcp.QueryMetricsService;
import dev.prospectos.api.mcp.QueryMetricsSnapshot;
import dev.prospectos.api.mcp.QueryTimeWindow;
import dev.prospectos.infrastructure.mcp.config.ConditionalOnMcpMockRuntime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@ConditionalOnMcpMockRuntime
public class DefaultQueryMetricsService implements QueryMetricsService {

    private static final List<String> PROVIDERS = List.of("nominatim", "bing-maps", "google-places", "scraper", "llm-discovery");
    private final Random random = new Random();

    @Override
    public QueryMetricsSnapshot getMetrics(QueryTimeWindow timeWindow, String provider) {
        log.debug("Generating mock metrics for timeWindow={} provider={}", timeWindow.value(), provider);
        var totalQueries = switch (timeWindow) {
            case ONE_HOUR -> 45 + random.nextInt(30);
            case TWENTY_FOUR_HOURS -> 800 + random.nextInt(400);
            case SEVEN_DAYS -> 4500 + random.nextInt(2000);
            case THIRTY_DAYS -> 18000 + random.nextInt(7000);
        };
        var totalCost = BigDecimal.valueOf(totalQueries * (0.003 + (random.nextDouble() * 0.005))).setScale(4, BigDecimal.ROUND_HALF_UP);
        var providers = provider != null ? List.of(provider) : PROVIDERS;
        return new QueryMetricsSnapshot(
            totalQueries,
            totalCost,
            totalQueries > 0 ? totalCost.divide(BigDecimal.valueOf(totalQueries), 4, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO,
            0.85 + (random.nextDouble() * 0.12),
            800 + random.nextInt(400),
            new QueryMetricsSnapshot.Trends(pickTrend(), pickTrend(), pickTrend()),
            providers.stream().map(currentProvider -> new QueryMetricsSnapshot.ProviderMetric(
                currentProvider,
                provider != null ? totalQueries : totalQueries / providers.size() + random.nextInt(50),
                provider != null ? totalCost : totalCost.divide(BigDecimal.valueOf(providers.size()), 4, BigDecimal.ROUND_HALF_UP),
                0.80 + (random.nextDouble() * 0.18),
                600 + random.nextInt(800)
            )).toList()
        );
    }

    private String pickTrend() {
        var trends = List.of("INCREASING", "STABLE", "DECREASING");
        return trends.get(random.nextInt(trends.size()));
    }
}




