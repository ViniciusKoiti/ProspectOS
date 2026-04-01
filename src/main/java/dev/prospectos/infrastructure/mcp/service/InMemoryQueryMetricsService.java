package dev.prospectos.infrastructure.mcp.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import dev.prospectos.api.mcp.QueryMetricsSnapshot;
import dev.prospectos.api.mcp.QueryTimeWindow;

@Service
@ConditionalOnProperty(prefix = "spring.ai.mcp.server", name = "enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryQueryMetricsService implements ObservedQueryMetricsService {
    private static final int MAX_OBSERVATIONS = 2_000;
    private final ConcurrentLinkedDeque<QueryMetricsObservation> observations;
    private final Clock clock;
    private final QueryMetricsCostEstimator costEstimator;
    private final QueryMetricsSnapshotFactory snapshotFactory;

    public InMemoryQueryMetricsService() {
        this(new ConcurrentLinkedDeque<>(), Clock.systemUTC(), new QueryMetricsCostEstimator(), new QueryMetricsSnapshotFactory());
    }

    InMemoryQueryMetricsService(ConcurrentLinkedDeque<QueryMetricsObservation> observations, Clock clock,
                                QueryMetricsCostEstimator costEstimator, QueryMetricsSnapshotFactory snapshotFactory) {
        this.observations = observations;
        this.clock = clock;
        this.costEstimator = costEstimator;
        this.snapshotFactory = snapshotFactory;
    }

    @Override
    public void recordExecution(String provider, long durationMs, boolean success, int resultCount) {
        recordExecution(provider, "lead-search", durationMs, success, resultCount);
    }

    @Override
    public void recordExecution(String provider, String operation, long durationMs, boolean success, int resultCount) {
        observations.addLast(new QueryMetricsObservation(
            normalize(provider), normalizeOperation(operation), clock.instant(), Math.max(durationMs, 0L), success,
            Math.max(resultCount, 0), costEstimator.estimate(normalize(provider), resultCount)
        ));
        trimToWindow();
    }

    @Override
    public QueryMetricsSnapshot getMetrics(QueryTimeWindow timeWindow, String provider) {
        return snapshotFactory.create(observations(timeWindow, provider), timeWindow, clock.instant());
    }

    @Override
    public List<QueryMetricsObservation> observations(QueryTimeWindow timeWindow, String provider) {
        Instant cutoff = clock.instant().minus(timeWindow.duration());
        String normalizedProvider = provider == null ? null : normalize(provider);
        return observations.stream()
            .filter(observation -> !observation.recordedAt().isBefore(cutoff))
            .filter(observation -> normalizedProvider == null || observation.provider().equals(normalizedProvider))
            .toList();
    }

    @Override
    public List<String> observedProviders(QueryTimeWindow timeWindow) {
        return observations(timeWindow, null).stream().map(QueryMetricsObservation::provider).distinct().sorted().toList();
    }

    private void trimToWindow() {
        while (observations.size() > MAX_OBSERVATIONS) observations.pollFirst();
        var cutoff = clock.instant().minus(QueryTimeWindow.THIRTY_DAYS.duration());
        while (!observations.isEmpty() && observations.peekFirst().recordedAt().isBefore(cutoff)) observations.pollFirst();
    }

    private String normalize(String provider) {
        return provider == null ? "unknown" : provider.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeOperation(String operation) {
        return operation == null || operation.isBlank() ? "lead-search" : operation.trim();
    }
}
