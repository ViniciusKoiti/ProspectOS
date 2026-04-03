package dev.prospectos.infrastructure.api.outreach;

import dev.prospectos.api.OutreachDeliveryService;
import dev.prospectos.api.dto.request.OutreachDeliveryRequest;
import dev.prospectos.api.dto.response.OutreachDeliveryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/outreach/deliveries")
public class OutreachDeliveryController {

    private final OutreachDeliveryService outreachDeliveryService;

    public OutreachDeliveryController(OutreachDeliveryService outreachDeliveryService) {
        this.outreachDeliveryService = outreachDeliveryService;
    }

    @PostMapping
    public ResponseEntity<OutreachDeliveryResponse> send(@RequestBody OutreachDeliveryRequest request) {
        return ResponseEntity.ok(outreachDeliveryService.send(request));
    }
}
