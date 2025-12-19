package dev.prospectos.core.domain.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain event fired when a company receives a new prospecting score.
 */
public record CompanyScored(
    UUID companyId,
    String companyName,
    BigDecimal previousScore,
    BigDecimal newScore,
    String scoreReason,
    Instant occurredOn
) {
    public CompanyScored(UUID companyId, String companyName, BigDecimal previousScore, 
                        BigDecimal newScore, String scoreReason) {
        this(companyId, companyName, previousScore, newScore, scoreReason, Instant.now());
    }
}