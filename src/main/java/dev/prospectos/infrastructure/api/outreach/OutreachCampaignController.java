package dev.prospectos.infrastructure.api.outreach;

import dev.prospectos.api.OutreachCampaignService;
import dev.prospectos.api.dto.request.OutreachCampaignCreateRequest;
import dev.prospectos.api.dto.response.OutreachCampaignResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Outreach campaign API endpoints.
 */
@RestController
@RequestMapping("/api/outreach/campaigns")
public class OutreachCampaignController {

    private final OutreachCampaignService outreachCampaignService;

    public OutreachCampaignController(OutreachCampaignService outreachCampaignService) {
        this.outreachCampaignService = outreachCampaignService;
    }

    @PostMapping
    public ResponseEntity<OutreachCampaignResponse> createCampaign(@RequestBody OutreachCampaignCreateRequest request) {
        OutreachCampaignResponse response = outreachCampaignService.startCampaign(request);
        return ResponseEntity.ok(response);
    }
}
