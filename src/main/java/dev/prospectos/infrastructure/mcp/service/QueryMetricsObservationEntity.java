package dev.prospectos.infrastructure.mcp.service;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mcp_query_observations")
public class QueryMetricsObservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String operation;

    @Column(nullable = false)
    private Instant recordedAt;

    @Column(nullable = false)
    private long durationMs;

    @Column(nullable = false)
    private boolean success;

    @Column(nullable = false)
    private int resultCount;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal estimatedCost;

    protected QueryMetricsObservationEntity() {
    }

    QueryMetricsObservationEntity(String provider, String operation, Instant recordedAt, long durationMs, boolean success, int resultCount, BigDecimal estimatedCost) {
        this.provider = provider;
        this.operation = operation;
        this.recordedAt = recordedAt;
        this.durationMs = durationMs;
        this.success = success;
        this.resultCount = resultCount;
        this.estimatedCost = estimatedCost;
    }

    QueryMetricsObservation toDomain() {
        return new QueryMetricsObservation(provider, operation, recordedAt, durationMs, success, resultCount, estimatedCost);
    }

    String provider() {
        return provider;
    }
}
