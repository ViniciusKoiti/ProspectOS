package dev.prospectos.infrastructure.api.outreach;

import dev.prospectos.api.OutreachCampaignService;
import dev.prospectos.api.dto.request.OutreachCampaignCreateRequest;
import dev.prospectos.api.dto.response.OutreachCampaignResponse;
import dev.prospectos.api.dto.response.OutreachLeadStatusResponse;
import dev.prospectos.infrastructure.handler.ApiExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OutreachCampaignControllerTest {

    @Mock
    private OutreachCampaignService outreachCampaignService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new OutreachCampaignController(outreachCampaignService))
            .setControllerAdvice(new ApiExceptionHandler())
            .build();
    }

    @Test
    void createCampaignSerializesPayload() throws Exception {
        UUID campaignId = UUID.fromString("123e4567-e89b-12d3-a456-426614174111");
        OutreachCampaignResponse response = new OutreachCampaignResponse(
            campaignId,
            OutreachCampaignCreateRequest.WebsitePresenceSegment.HAS_WEBSITE,
            List.of(new OutreachLeadStatusResponse(
                10L,
                "Acme",
                "https://acme.com",
                OutreachLeadStatusResponse.DeliveryStatus.SENT,
                "Message sent"
            )),
            1,
            0,
            0,
            "Outreach campaign started"
        );
        when(outreachCampaignService.startCampaign(any(OutreachCampaignCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/outreach/campaigns")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "segment": "HAS_WEBSITE",
                      "limit": 25
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.campaignId").value(campaignId.toString()))
            .andExpect(jsonPath("$.segment").value("HAS_WEBSITE"))
            .andExpect(jsonPath("$.sent").value(1))
            .andExpect(jsonPath("$.leads[0].companyId").value(10))
            .andExpect(jsonPath("$.leads[0].status").value("SENT"));
    }
}
