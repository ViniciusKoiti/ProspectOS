package dev.prospectos.infrastructure.mcp.service;

import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.stereotype.Service;

import dev.prospectos.api.mcp.QueryMetricsRecorder;
import dev.prospectos.api.mcp.QueryMetricsService;
import dev.prospectos.api.mcp.QueryMetricsSnapshot;
import dev.prospectos.api.mcp.QueryTimeWindow;

@Service
public class InMemoryQueryMetricsService implements QueryMetricsService, QueryMetricsRecorder {

    private static final int MAX_OBSERVATIONS = 2_000;

    private final ConcurrentLinkedDeque<QueryMetricsObservation> observations;
    private final Clock clock;
    private final QueryMetricsCostEstimator costEstimator;
    private final QueryMetricsSnapshotFactory snapshotFactory;

    public InMemoryQueryMetricsService() {
        this(new ConcurrentLinkedDeque<>(), Clock.systemUTC(), new QueryMetricsCostEstimator(), new QueryMetricsSnapshotFactory());
    }

    InMemoryQueryMetricsService(
        ConcurrentLinkedDeque<QueryMetricsObservation> observations,
        Clock clock,
        QueryMetricsCostEstimator costEstimator,
        QueryMetricsSnapshotFactory snapshotFactory
    ) {
        this.observations = observations;
        this.clock = clock;
        this.costEstimator = costEstimator;
        this.snapshotFactory = snapshotFactory;
    }

    @Override
    public void recordExecution(String provider, long durationMs, boolean success, int resultCount) {
        String normalizedProvider = normalize(provider);
        observations.addLast(new QueryMetricsObservation(
            normalizedProvider,
            clock.instant(),
            Math.max(durationMs, 0L),
            success,
            Math.max(resultCount, 0),
            costEstimator.estimate(normalizedProvider, resultCount)
        ));
        trimToWindow();
    }

    @Override
    public QueryMetricsSnapshot getMetrics(QueryTimeWindow timeWindow, String provider) {
        String normalizedProvider = provider == null ? null : normalize(provider);
        List<QueryMetricsObservation> filtered = observations.stream()
            .filter(observation -> normalizedProvider == null || observation.provider().equals(normalizedProvider))
            .toList();
        return snapshotFactory.create(filtered, timeWindow, clock.instant());
    }

    private void trimToWindow() {
        while (observations.size() > MAX_OBSERVATIONS) {
            observations.pollFirst();
        }
        var cutoff = clock.instant().minus(QueryTimeWindow.THIRTY_DAYS.duration());
        while (!observations.isEmpty() && observations.peekFirst().recordedAt().isBefore(cutoff)) {
            observations.pollFirst();
        }
    }

    private String normalize(String provider) {
        return provider == null ? "unknown" : provider.trim().toLowerCase(Locale.ROOT);
    }
}