package dev.prospectos.infrastructure.service.discovery;

import java.util.List;

import dev.prospectos.api.dto.CompanyCandidateDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiscoveryCandidateMapperTest {

    private final DiscoveryCandidateMapper mapper = new DiscoveryCandidateMapper();

    @Test
    void toCandidate_shouldMarkNoWebsiteWhenWebsiteIsMissing() {
        DiscoveredLeadCandidate candidate = mapper.toCandidate(
            "llm-discovery",
            "Acme Foods",
            null,
            "Food Distribution",
            "Food supplier",
            "Curitiba",
            List.of("sales@acme.com")
        );

        assertThat(candidate).isNotNull();
        assertThat(candidate.website()).isNull();
        assertThat(candidate.websitePresence()).isEqualTo(CompanyCandidateDTO.WebsitePresence.NO_WEBSITE);
        assertThat(candidate.contacts()).containsExactly("sales@acme.com");
    }

    @Test
    void toCandidate_shouldMarkUnknownWhenWebsiteIsInvalid() {
        DiscoveredLeadCandidate candidate = mapper.toCandidate(
            "llm-discovery",
            "Acme Foods",
            "acme foods",
            "Food Distribution",
            "Food supplier",
            "Curitiba",
            List.of("sales@acme.com")
        );

        assertThat(candidate).isNotNull();
        assertThat(candidate.website()).isNull();
        assertThat(candidate.websitePresence()).isEqualTo(CompanyCandidateDTO.WebsitePresence.UNKNOWN);
    }

    @Test
    void toCandidate_shouldNormalizeWebsiteAndMarkHasWebsite() {
        DiscoveredLeadCandidate candidate = mapper.toCandidate(
            "llm-discovery",
            "Acme Foods",
            "acmefoods.com",
            "Food Distribution",
            "Food supplier",
            "Curitiba",
            List.of("sales@acme.com")
        );

        assertThat(candidate).isNotNull();
        assertThat(candidate.website()).isEqualTo("https://acmefoods.com");
        assertThat(candidate.websitePresence()).isEqualTo(CompanyCandidateDTO.WebsitePresence.HAS_WEBSITE);
    }
}
