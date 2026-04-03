package dev.prospectos.api.dto.response;

public record OutreachDeliveryResponse(
    String deliveryId,
    String status,
    String message
) {
}
