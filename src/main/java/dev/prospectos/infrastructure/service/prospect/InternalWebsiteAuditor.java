package dev.prospectos.infrastructure.service.prospect;

import dev.prospectos.ai.client.ScrapingResponse;
import dev.prospectos.api.dto.ProspectWebsiteAuditResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public final class InternalWebsiteAuditor {

    private final ProspectWebsiteAuditStatusPolicy statusPolicy;

    public InternalWebsiteAuditor(ProspectWebsiteAuditStatusPolicy statusPolicy) {
        this.statusPolicy = statusPolicy;
    }

    ProspectWebsiteAuditResponse audit(String website, ScrapingResponse response) {
        int score = 100;
        List<String> findings = new ArrayList<>();
        boolean secure = website != null && website.startsWith("https://");
        boolean scrapeSucceeded = response != null && response.success() && response.data() != null;

        if (!secure) {
            score -= 20;
            findings.add("Website is not using HTTPS.");
        }
        if (!scrapeSucceeded) {
            score -= 35;
            findings.add("Website content could not be extracted reliably.");
            return new ProspectWebsiteAuditResponse(
                normalize(score),
                statusPolicy.status(normalize(score)),
                secure,
                false,
                false,
                false,
                null,
                findings
            );
        }

        Map<String, Object> data = response.data();
        boolean contactInfoDetected = hasValue(data, "emails") || hasValue(data, "email") || hasValue(data, "phone") || hasValue(data, "phones");
        boolean technologySignalsDetected = hasValue(data, "technologies") || hasValue(data, "tech_stack") || hasValue(data, "frameworks");
        boolean contentSignalsDetected = hasValue(data, "description") || hasValue(data, "meta_description")
            || hasValue(data, "title") || hasValue(data, "company_name") || hasValue(data, "about");

        if (!contactInfoDetected) {
            score -= 20;
            findings.add("No contact signals were detected in the extracted content.");
        }
        if (!technologySignalsDetected) {
            score -= 10;
            findings.add("No technology signals were detected in the extracted content.");
        }
        if (!contentSignalsDetected) {
            score -= 15;
            findings.add("Low descriptive content was extracted from the website.");
        }
        if (findings.isEmpty()) {
            findings.add("Website exposes enough signals for an initial sales review.");
        }

        int normalizedScore = normalize(score);
        return new ProspectWebsiteAuditResponse(
            normalizedScore,
            statusPolicy.status(normalizedScore),
            secure,
            true,
            contactInfoDetected,
            technologySignalsDetected,
            null,
            findings
        );
    }

    private boolean hasValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof String text) {
            return !text.isBlank();
        }
        if (value instanceof List<?> list) {
            return !list.isEmpty();
        }
        return value != null;
    }

    private int normalize(int score) {
        return Math.max(0, Math.min(score, 100));
    }
}
