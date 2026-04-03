package dev.prospectos.api;

import dev.prospectos.api.dto.request.OutreachDeliveryRequest;
import dev.prospectos.api.dto.response.OutreachDeliveryResponse;

public interface OutreachDeliveryService {
    OutreachDeliveryResponse send(OutreachDeliveryRequest request);
}
