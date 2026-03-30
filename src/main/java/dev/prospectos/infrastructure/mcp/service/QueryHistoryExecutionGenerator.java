package dev.prospectos.infrastructure.mcp.service;

import dev.prospectos.api.mcp.QueryExecution;
import dev.prospectos.api.mcp.QueryHistoryData;
import dev.prospectos.api.mcp.QueryTimeWindow;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

final class QueryHistoryExecutionGenerator {

    private static final List<String> PROVIDERS = List.of("nominatim", "bing-maps", "google-places", "scraper", "llm-discovery");
    private static final List<String> QUERIES = List.of("restaurants in Sao Paulo, Brazil", "tech companies in Buenos Aires", "hospitals in Mexico City", "banks in Madrid, Spain", "consultories in Rome, Italy");
    private static final List<String> ERRORS = List.of("Rate limit exceeded", "Connection timeout", "Invalid API key", "Service temporarily unavailable", "Query quota exceeded");
    private final Random random;

    QueryHistoryExecutionGenerator(Random random) {
        this.random = random;
    }

    QueryHistoryData generate(QueryTimeWindow timeWindow, String provider) {
        var providers = provider != null ? List.of(provider) : PROVIDERS;
        var executions = IntStream.range(0, executionCount(timeWindow)).mapToObj(index -> execution(index, providers)).toList();
        return new QueryHistoryData(timeWindow, provider, executions, aggregatedMetrics(executions));
    }

    private QueryExecution execution(int index, List<String> providers) {
        var success = random.nextDouble() > 0.15;
        return new QueryExecution(
            LocalDateTime.now().minusHours(index).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            QUERIES.get(random.nextInt(QUERIES.size())),
            providers.get(random.nextInt(providers.size())),
            success,
            success ? 500 + random.nextInt(1000) : 5000,
            success ? 0.001 + (random.nextDouble() * 0.009) : 0.0,
            success ? null : ERRORS.get(random.nextInt(ERRORS.size()))
        );
    }

    private Map<String, Object> aggregatedMetrics(List<QueryExecution> executions) {
        var totalQueries = executions.size();
        var successfulQueries = executions.stream().mapToInt(execution -> execution.success() ? 1 : 0).sum();
        var totalCost = executions.stream().mapToDouble(QueryExecution::cost).sum();
        var averageResponseTime = executions.stream().filter(QueryExecution::success).mapToLong(QueryExecution::responseTimeMs).average().orElse(0.0);
        var metrics = new HashMap<String, Object>();
        metrics.put("totalQueries", totalQueries);
        metrics.put("successfulQueries", successfulQueries);
        metrics.put("successRate", totalQueries > 0 ? (double) successfulQueries / totalQueries : 0.0);
        metrics.put("totalCost", totalCost);
        metrics.put("averageResponseTime", averageResponseTime);
        metrics.put("costPerQuery", totalQueries > 0 ? totalCost / totalQueries : 0.0);
        return metrics;
    }

    private int executionCount(QueryTimeWindow timeWindow) {
        return switch (timeWindow) {
            case ONE_HOUR -> 20 + random.nextInt(30);
            case TWENTY_FOUR_HOURS -> 100 + random.nextInt(100);
            case SEVEN_DAYS -> 500 + random.nextInt(300);
            case THIRTY_DAYS -> 2000 + random.nextInt(1000);
        };
    }
}
