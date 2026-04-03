package dev.prospectos.api.dto.request;

public record OutreachDeliveryRequest(
    String from,
    String to,
    String subject,
    String html,
    String text,
    String replyTo
) {
    public OutreachDeliveryRequest {
        if (isBlank(from)) {
            throw new IllegalArgumentException("from is required");
        }
        if (isBlank(to)) {
            throw new IllegalArgumentException("to is required");
        }
        if (isBlank(subject)) {
            throw new IllegalArgumentException("subject is required");
        }
        if (isBlank(html) && isBlank(text)) {
            throw new IllegalArgumentException("html or text is required");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
