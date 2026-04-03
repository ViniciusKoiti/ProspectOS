package dev.prospectos.infrastructure.service.prospect;

import java.util.List;

public record PageSpeedAuditResult(
    Integer score,
    List<String> findings
) {
    public static PageSpeedAuditResult unavailable() {
        return new PageSpeedAuditResult(null, List.of());
    }

    public boolean available() {
        return score != null;
    }
}
