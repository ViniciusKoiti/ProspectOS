package dev.prospectos.infrastructure.mcp.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

import dev.prospectos.api.mcp.QueryMetricsSnapshot;
import dev.prospectos.api.mcp.QueryTimeWindow;
import dev.prospectos.infrastructure.mcp.config.ConditionalOnMcpEnabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnMcpEnabled
public class JpaQueryMetricsService implements ObservedQueryMetricsService {

    private final QueryMetricsObservationRepository repository;
    private final Clock clock;
    private final QueryMetricsCostEstimator costEstimator;
    private final QueryMetricsSnapshotFactory snapshotFactory;

    @Autowired
    public JpaQueryMetricsService(QueryMetricsObservationRepository repository) {
        this(repository, Clock.systemUTC(), new QueryMetricsCostEstimator(), new QueryMetricsSnapshotFactory());
    }

    JpaQueryMetricsService(
        QueryMetricsObservationRepository repository,
        Clock clock,
        QueryMetricsCostEstimator costEstimator,
        QueryMetricsSnapshotFactory snapshotFactory
    ) {
        this.repository = repository;
        this.clock = clock;
        this.costEstimator = costEstimator;
        this.snapshotFactory = snapshotFactory;
    }

    @Override
    @Transactional
    public void recordExecution(String provider, long durationMs, boolean success, int resultCount) {
        recordExecution(provider, "lead-search", durationMs, success, resultCount);
    }

    @Override
    @Transactional
    public void recordExecution(String provider, String operation, long durationMs, boolean success, int resultCount) {
        repository.save(new QueryMetricsObservationEntity(
            normalize(provider),
            normalizeOperation(operation),
            clock.instant(),
            Math.max(durationMs, 0L),
            success,
            Math.max(resultCount, 0),
            costEstimator.estimate(normalize(provider), resultCount)
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public QueryMetricsSnapshot getMetrics(QueryTimeWindow timeWindow, String provider) {
        return snapshotFactory.create(observations(timeWindow, provider), timeWindow, clock.instant());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QueryMetricsObservation> observations(QueryTimeWindow timeWindow, String provider) {
        Instant cutoff = clock.instant().minus(timeWindow.duration());
        return entities(cutoff, provider).stream().map(QueryMetricsObservationEntity::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> observedProviders(QueryTimeWindow timeWindow) {
        Instant cutoff = clock.instant().minus(timeWindow.duration());
        return repository.findAllByRecordedAtAfterOrderByRecordedAtDesc(cutoff).stream()
            .map(QueryMetricsObservationEntity::provider)
            .distinct()
            .sorted()
            .toList();
    }

    private List<QueryMetricsObservationEntity> entities(Instant cutoff, String provider) {
        String normalizedProvider = provider == null ? null : normalize(provider);
        return normalizedProvider == null
            ? repository.findAllByRecordedAtAfterOrderByRecordedAtDesc(cutoff)
            : repository.findAllByRecordedAtAfterAndProviderOrderByRecordedAtDesc(cutoff, normalizedProvider);
    }

    private String normalize(String provider) {
        return provider == null ? "unknown" : provider.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeOperation(String operation) {
        return operation == null || operation.isBlank() ? "lead-search" : operation.trim();
    }
}




