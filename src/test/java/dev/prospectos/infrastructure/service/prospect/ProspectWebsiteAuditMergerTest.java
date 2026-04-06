package dev.prospectos.infrastructure.service.prospect;

import java.util.List;

import dev.prospectos.api.dto.ProspectWebsiteAuditResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProspectWebsiteAuditMergerTest {

    private final ProspectWebsiteAuditMerger merger = new ProspectWebsiteAuditMerger(new ProspectWebsiteAuditStatusPolicy());

    @Test
    void mergesInternalAuditWithPagespeedResult() {
        var base = new ProspectWebsiteAuditResponse(80, "GOOD", true, true, true, true, null, List.of("Internal"));
        var merged = merger.merge(base, new PageSpeedAuditResult(40, List.of("PageSpeed")));

        assertThat(merged.score()).isEqualTo(56);
        assertThat(merged.status()).isEqualTo("REVIEW");
        assertThat(merged.pageSpeedScore()).isEqualTo(40);
        assertThat(merged.findings()).containsExactly("Internal", "PageSpeed");
    }

    @Test
    void keepsBaseAuditWhenPagespeedIsUnavailable() {
        var base = new ProspectWebsiteAuditResponse(72, "REVIEW", true, true, false, false, null, List.of("Internal"));

        assertThat(merger.merge(base, PageSpeedAuditResult.unavailable())).isEqualTo(base);
    }
}
