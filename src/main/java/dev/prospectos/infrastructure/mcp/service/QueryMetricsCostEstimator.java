package dev.prospectos.infrastructure.mcp.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

final class QueryMetricsCostEstimator {

    private static final Map<String, BigDecimal> COST_BY_PROVIDER = Map.of(
        "amazon-location", new BigDecimal("0.0350"),
        "google-places", new BigDecimal("0.0170"),
        "llm-discovery", new BigDecimal("0.0200"),
        "scraper", new BigDecimal("0.0125"),
        "vector-company", new BigDecimal("0.0008"),
        "cnpj-ws", BigDecimal.ZERO,
        "open-cnpj", BigDecimal.ZERO,
        "in-memory", BigDecimal.ZERO
    );

    BigDecimal estimate(String provider, int resultCount) {
        BigDecimal baseCost = COST_BY_PROVIDER.getOrDefault(provider, BigDecimal.ZERO);
        if (baseCost.signum() == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return baseCost.multiply(BigDecimal.valueOf(Math.max(resultCount, 1)))
            .setScale(2, RoundingMode.HALF_UP);
    }
}
