package dev.prospectos.infrastructure.mcp.service;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QueryMetricsObservationEntityTest {

    @Test
    void convertsEntityToDomainObservation() {
        var entity = new QueryMetricsObservationEntity(
            "in-memory",
            "software",
            Instant.parse("2026-03-30T18:00:00Z"),
            120,
            true,
            2,
            new BigDecimal("0.0100")
        );

        var observation = entity.toDomain();

        assertThat(entity.provider()).isEqualTo("in-memory");
        assertThat(observation.provider()).isEqualTo("in-memory");
        assertThat(observation.operation()).isEqualTo("software");
        assertThat(observation.durationMs()).isEqualTo(120);
    }
}
