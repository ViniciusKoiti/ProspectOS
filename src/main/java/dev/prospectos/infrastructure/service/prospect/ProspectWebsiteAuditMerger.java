package dev.prospectos.infrastructure.service.prospect;

import java.util.ArrayList;
import java.util.List;

import dev.prospectos.api.dto.ProspectWebsiteAuditResponse;

final class ProspectWebsiteAuditMerger {

    ProspectWebsiteAuditResponse merge(ProspectWebsiteAuditResponse base, PageSpeedAuditResult pageSpeed) {
        if (pageSpeed == null || !pageSpeed.available()) {
            return base;
        }
        int mergedScore = (int) Math.round((base.score() * 0.4) + (pageSpeed.score() * 0.6));
        List<String> findings = new ArrayList<>(base.findings());
        findings.addAll(pageSpeed.findings());
        return new ProspectWebsiteAuditResponse(
            mergedScore,
            status(mergedScore),
            base.secure(),
            base.scrapeSucceeded(),
            base.contactInfoDetected(),
            base.technologySignalsDetected(),
            pageSpeed.score(),
            findings
        );
    }

    private String status(int score) {
        if (score >= 80) {
            return "GOOD";
        }
        if (score >= 55) {
            return "REVIEW";
        }
        return "POOR";
    }
}
