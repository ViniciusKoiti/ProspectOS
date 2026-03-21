package dev.prospectos.infrastructure.service.outreach;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.request.OutreachCampaignCreateRequest;
import dev.prospectos.api.dto.response.OutreachCampaignResponse;
import dev.prospectos.api.dto.response.OutreachLeadStatusResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InMemoryOutreachCampaignServiceTest {

    @Mock
    private CompanyDataService companyDataService;

    @Test
    void startCampaignFiltersHasWebsiteAndComputesCounters() {
        when(companyDataService.findAllCompanies()).thenReturn(List.of(
            company(1L, "Alpha", "https://alpha.com", "sales@alpha.com", 1, 91),
            company(2L, "Beta", "https://beta.com", null, 0, 88),
            company(3L, "Gamma", null, "team@gamma.com", 1, 60),
            company(4L, "Delta", "https://delta.com", "contact@delta.com", 1, 70)
        ));
        InMemoryOutreachCampaignService service = new InMemoryOutreachCampaignService(companyDataService);

        OutreachCampaignResponse response = service.startCampaign(
            new OutreachCampaignCreateRequest(OutreachCampaignCreateRequest.WebsitePresenceSegment.HAS_WEBSITE, 50)
        );

        assertThat(response.leads()).hasSize(3);
        assertThat(response.sent()).isEqualTo(2);
        assertThat(response.failures()).isEqualTo(1);
        assertThat(response.responses()).isEqualTo(1);

        Map<Long, OutreachLeadStatusResponse.DeliveryStatus> statuses = response.leads().stream()
            .collect(Collectors.toMap(OutreachLeadStatusResponse::companyId, OutreachLeadStatusResponse::status));
        assertThat(statuses.get(1L)).isEqualTo(OutreachLeadStatusResponse.DeliveryStatus.REPLIED);
        assertThat(statuses.get(2L)).isEqualTo(OutreachLeadStatusResponse.DeliveryStatus.FAILED);
        assertThat(statuses.get(4L)).isEqualTo(OutreachLeadStatusResponse.DeliveryStatus.SENT);
    }

    @Test
    void startCampaignFiltersNoWebsiteSegment() {
        when(companyDataService.findAllCompanies()).thenReturn(List.of(
            company(1L, "Alpha", "https://alpha.com", "sales@alpha.com", 1, 91),
            company(2L, "Beta", null, "contato@beta.com", 1, 72),
            company(3L, "Gamma", " ", null, 0, 20)
        ));
        InMemoryOutreachCampaignService service = new InMemoryOutreachCampaignService(companyDataService);

        OutreachCampaignResponse response = service.startCampaign(
            new OutreachCampaignCreateRequest(OutreachCampaignCreateRequest.WebsitePresenceSegment.NO_WEBSITE, 50)
        );

        assertThat(response.segment()).isEqualTo(OutreachCampaignCreateRequest.WebsitePresenceSegment.NO_WEBSITE);
        assertThat(response.leads()).extracting(OutreachLeadStatusResponse::companyId).containsExactly(2L, 3L);
    }

    @Test
    void startCampaignRejectsNullSegment() {
        InMemoryOutreachCampaignService service = new InMemoryOutreachCampaignService(companyDataService);

        assertThatThrownBy(() -> service.startCampaign(new OutreachCampaignCreateRequest(null, 10)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("segment is required");
    }

    private CompanyDTO company(
        Long id,
        String name,
        String website,
        String primaryContactEmail,
        Integer contactCount,
        int score
    ) {
        return new CompanyDTO(
            id,
            name,
            "Software",
            website,
            "description",
            25,
            "Sao Paulo",
            new ScoreDTO(score, "HOT", "fit"),
            primaryContactEmail,
            contactCount
        );
    }
}

