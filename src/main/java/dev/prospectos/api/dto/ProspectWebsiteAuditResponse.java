package dev.prospectos.api.dto;

import java.util.List;

public record ProspectWebsiteAuditResponse(
    int score,
    String status,
    boolean secure,
    boolean scrapeSucceeded,
    boolean contactInfoDetected,
    boolean technologySignalsDetected,
    List<String> findings
) {
}
