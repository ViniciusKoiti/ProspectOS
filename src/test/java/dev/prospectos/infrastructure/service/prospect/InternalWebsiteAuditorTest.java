package dev.prospectos.infrastructure.service.prospect;

import dev.prospectos.ai.client.ScrapingResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class InternalWebsiteAuditorTest {

    private final InternalWebsiteAuditor auditor = new InternalWebsiteAuditor();

    @Test
    void auditReturnsGoodWhenSecureAndRichSignalsExist() {
        var response = new ScrapingResponse(true, Map.of(
            "emails", List.of("sales@example.com"),
            "technologies", List.of("Spring Boot"),
            "description", "Modern responsive company site"
        ), null);

        var result = auditor.audit("https://example.com", response);

        assertThat(result.score()).isEqualTo(100);
        assertThat(result.status()).isEqualTo("GOOD");
        assertThat(result.secure()).isTrue();
        assertThat(result.scrapeSucceeded()).isTrue();
        assertThat(result.contactInfoDetected()).isTrue();
        assertThat(result.technologySignalsDetected()).isTrue();
    }

    @Test
    void auditReturnsPoorWhenScrapeFails() {
        var result = auditor.audit("http://legacy.example.com", new ScrapingResponse(false, null, "timeout"));

        assertThat(result.score()).isEqualTo(45);
        assertThat(result.status()).isEqualTo("POOR");
        assertThat(result.secure()).isFalse();
        assertThat(result.scrapeSucceeded()).isFalse();
        assertThat(result.findings()).contains("Website is not using HTTPS.");
    }

    @Test
    void auditReturnsReviewWhenSignalsArePartial() {
        var response = new ScrapingResponse(true, Map.of(
            "description", "Static brochure site"
        ), null);

        var result = auditor.audit("https://brochure.example.com", response);

        assertThat(result.score()).isEqualTo(70);
        assertThat(result.status()).isEqualTo("REVIEW");
        assertThat(result.contactInfoDetected()).isFalse();
        assertThat(result.technologySignalsDetected()).isFalse();
    }
}
