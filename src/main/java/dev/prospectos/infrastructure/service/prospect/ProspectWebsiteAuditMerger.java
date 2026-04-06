package dev.prospectos.infrastructure.service.prospect;

import java.util.ArrayList;
import java.util.List;

import dev.prospectos.api.dto.ProspectWebsiteAuditResponse;
import org.springframework.stereotype.Component;

@Component
public final class ProspectWebsiteAuditMerger {

    private final ProspectWebsiteAuditStatusPolicy statusPolicy;

    public ProspectWebsiteAuditMerger(ProspectWebsiteAuditStatusPolicy statusPolicy) {
        this.statusPolicy = statusPolicy;
    }

    ProspectWebsiteAuditResponse merge(ProspectWebsiteAuditResponse base, PageSpeedAuditResult pageSpeed) {
        if (pageSpeed == null || !pageSpeed.available()) {
            return base;
        }
        int mergedScore = (int) Math.round((base.score() * 0.4) + (pageSpeed.score() * 0.6));
        List<String> findings = new ArrayList<>(base.findings());
        findings.addAll(pageSpeed.findings());
        return new ProspectWebsiteAuditResponse(
            mergedScore,
            statusPolicy.status(mergedScore),
            base.secure(),
            base.scrapeSucceeded(),
            base.contactInfoDetected(),
            base.technologySignalsDetected(),
            pageSpeed.score(),
            findings
        );
    }
}
