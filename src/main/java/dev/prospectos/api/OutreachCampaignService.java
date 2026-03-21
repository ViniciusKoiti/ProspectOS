package dev.prospectos.api;

import dev.prospectos.api.dto.request.OutreachCampaignCreateRequest;
import dev.prospectos.api.dto.response.OutreachCampaignResponse;

/**
 * Public interface to start outreach campaigns.
 */
public interface OutreachCampaignService {

    OutreachCampaignResponse startCampaign(OutreachCampaignCreateRequest request);
}
