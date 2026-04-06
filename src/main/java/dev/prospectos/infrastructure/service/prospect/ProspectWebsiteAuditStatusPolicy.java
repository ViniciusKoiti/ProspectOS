package dev.prospectos.infrastructure.service.prospect;

import org.springframework.stereotype.Component;

@Component
public final class ProspectWebsiteAuditStatusPolicy {

    public String status(int score) {
        if (score >= 80) {
            return "GOOD";
        }
        if (score >= 55) {
            return "REVIEW";
        }
        return "POOR";
    }
}
