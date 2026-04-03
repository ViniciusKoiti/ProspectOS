package dev.prospectos.infrastructure.service.outreach;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.prospectos.api.dto.request.OutreachDeliveryRequest;

record ResendEmailRequest(
    String from,
    String to,
    String subject,
    String html,
    String text,
    @JsonProperty("reply_to")
    String replyTo
) {
    static ResendEmailRequest from(OutreachDeliveryRequest request) {
        return new ResendEmailRequest(
            request.from().trim(),
            request.to().trim(),
            request.subject().trim(),
            blankToNull(request.html()),
            blankToNull(request.text()),
            blankToNull(request.replyTo())
        );
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
