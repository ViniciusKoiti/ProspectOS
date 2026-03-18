package dev.prospectos.infrastructure.service.discovery;

import java.util.List;

import dev.prospectos.api.dto.CompanyCandidateDTO;
import dev.prospectos.api.dto.LeadResultDTO;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.util.LeadKeyGenerator;
import dev.prospectos.infrastructure.service.scoring.CompanyScoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DiscoveryLeadResultFactoryTest {

    private CompanyScoringService scoringService;
    private DiscoveryLeadResultFactory factory;

    @BeforeEach
    void setUp() {
        scoringService = mock(CompanyScoringService.class);
        factory = new DiscoveryLeadResultFactory(scoringService, "llm-discovery");
    }

    @Test
    void toLeadResults_shouldBuildLeadForCandidateWithoutWebsite() {
        ICP icp = icp();
        when(scoringService.scoreCandidate(any(Company.class), eq(icp)))
            .thenReturn(new ScoreDTO(72, "WARM", "fit"));

        DiscoveredLeadCandidate candidate = new DiscoveredLeadCandidate(
            "Acme Foods",
            null,
            "food distribution",
            "Regional supplier",
            "Curitiba",
            List.of("sales@acme.com"),
            "llm-discovery",
            CompanyCandidateDTO.WebsitePresence.NO_WEBSITE
        );

        List<LeadResultDTO> leads = factory.toLeadResults(List.of(candidate), icp, 10);

        assertThat(leads).hasSize(1);
        assertThat(leads.getFirst().candidate().website()).isNull();
        assertThat(leads.getFirst().candidate().websitePresence())
            .isEqualTo(CompanyCandidateDTO.WebsitePresence.NO_WEBSITE);
        assertThat(leads.getFirst().leadKey())
            .isEqualTo(LeadKeyGenerator.generate(null, "llm-discovery", "Acme Foods"));
        assertThat(leads.getFirst().source().sourceUrl()).isNull();
        var companyCaptor = forClass(Company.class);
        verify(scoringService).scoreCandidate(companyCaptor.capture(), eq(icp));
        assertThat(companyCaptor.getValue().getWebsite()).isNotNull();
        assertThat(companyCaptor.getValue().getWebsite().getUrl())
            .contains("no-website.preview.local");
    }

    @Test
    void toLeadResults_shouldPreserveWebsiteBasedKeys() {
        ICP icp = icp();
        when(scoringService.scoreCandidate(any(Company.class), eq(icp)))
            .thenReturn(new ScoreDTO(80, "HOT", "fit"));

        DiscoveredLeadCandidate candidate = new DiscoveredLeadCandidate(
            "Vector Co",
            "https://vector.example",
            "software",
            "Software company",
            "Sao Paulo",
            List.of(),
            "vector-company",
            CompanyCandidateDTO.WebsitePresence.HAS_WEBSITE
        );

        List<LeadResultDTO> leads = factory.toLeadResults(List.of(candidate), icp, 10);

        assertThat(leads).hasSize(1);
        assertThat(leads.getFirst().candidate().website()).isEqualTo("https://vector.example");
        assertThat(leads.getFirst().candidate().websitePresence())
            .isEqualTo(CompanyCandidateDTO.WebsitePresence.HAS_WEBSITE);
        assertThat(leads.getFirst().leadKey())
            .isEqualTo(LeadKeyGenerator.generate("https://vector.example", "vector-company"));
    }

    private ICP icp() {
        return ICP.create("ICP", "desc", List.of("software"), List.of("brazil"), List.of("cto"), "growth");
    }
}
