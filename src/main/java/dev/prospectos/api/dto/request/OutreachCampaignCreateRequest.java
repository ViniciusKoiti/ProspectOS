package dev.prospectos.api.dto.request;

/**
 * Request payload to start an outreach campaign.
 */
public record OutreachCampaignCreateRequest(
    WebsitePresenceSegment segment,
    Integer limit
) {

    public OutreachCampaignCreateRequest {
        if (segment == null) {
            throw new IllegalArgumentException("segment is required");
        }
        if (limit != null && (limit < 1 || limit > 500)) {
            throw new IllegalArgumentException("limit must be between 1 and 500");
        }
    }

    public enum WebsitePresenceSegment {
        ALL,
        HAS_WEBSITE,
        NO_WEBSITE
    }
}
