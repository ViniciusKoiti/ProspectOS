package dev.prospectos.api.dto.response;

/**
 * Per-lead outreach delivery status.
 */
public record OutreachLeadStatusResponse(
    Long companyId,
    String companyName,
    String website,
    DeliveryStatus status,
    String message
) {

    public enum DeliveryStatus {
        SENT,
        FAILED,
        REPLIED
    }
}
