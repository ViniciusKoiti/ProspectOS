package dev.prospectos.infrastructure.mcp.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JpaQueryMetricsServiceTest {

    private final QueryMetricsObservationRepository repository = mock(QueryMetricsObservationRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-03-30T18:00:00Z"), ZoneOffset.UTC);
    private final JpaQueryMetricsService service = new JpaQueryMetricsService(
        repository,
        clock,
        new QueryMetricsCostEstimator(),
        new QueryMetricsSnapshotFactory()
    );

    @Test
    void recordsNormalizedObservation() {
        service.recordExecution("In-Memory", "software", 250, true, 3);

        verify(repository).save(any(QueryMetricsObservationEntity.class));
    }

    @Test
    void readsPersistedObservationsAndBuildsSnapshot() {
        when(repository.findAllByRecordedAtAfterAndProviderOrderByRecordedAtDesc(any(), any())).thenReturn(List.of(
            new QueryMetricsObservationEntity("in-memory", "software", clock.instant(), 250, true, 3, new BigDecimal("0.0000"))
        ));
        when(repository.findAllByRecordedAtAfterOrderByRecordedAtDesc(any())).thenReturn(List.of(
            new QueryMetricsObservationEntity("in-memory", "software", clock.instant(), 250, true, 3, new BigDecimal("0.0000"))
        ));

        var history = service.observations(dev.prospectos.api.mcp.QueryTimeWindow.ONE_HOUR, "in-memory");
        var snapshot = service.getMetrics(dev.prospectos.api.mcp.QueryTimeWindow.ONE_HOUR, "in-memory");

        assertThat(history).hasSize(1);
        assertThat(history.getFirst().operation()).isEqualTo("software");
        assertThat(snapshot.totalQueries()).isEqualTo(1);
        assertThat(service.observedProviders(dev.prospectos.api.mcp.QueryTimeWindow.ONE_HOUR)).containsExactly("in-memory");
    }
}
