package dev.prospectos.infrastructure.mcp.service;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QueryMetricsObservationRepository extends JpaRepository<QueryMetricsObservationEntity, Long> {

    List<QueryMetricsObservationEntity> findAllByRecordedAtAfterOrderByRecordedAtDesc(Instant cutoff);

    List<QueryMetricsObservationEntity> findAllByRecordedAtAfterAndProviderOrderByRecordedAtDesc(Instant cutoff, String provider);
}
