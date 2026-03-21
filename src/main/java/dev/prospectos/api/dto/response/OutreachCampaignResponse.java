package dev.prospectos.api.dto.response;

import dev.prospectos.api.dto.request.OutreachCampaignCreateRequest;

import java.util.List;
import java.util.UUID;

/**
 * Response payload after starting an outreach campaign.
 */
public record OutreachCampaignResponse(
    UUID campaignId,
    OutreachCampaignCreateRequest.WebsitePresenceSegment segment,
    List<OutreachLeadStatusResponse> leads,
    int sent,
    int failures,
    int responses,
    String message
) {
}
