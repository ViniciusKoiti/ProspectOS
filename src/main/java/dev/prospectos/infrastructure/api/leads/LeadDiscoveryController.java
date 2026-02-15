package dev.prospectos.infrastructure.api.leads;

import dev.prospectos.api.LeadDiscoveryService;
import dev.prospectos.api.dto.LeadDiscoveryRequest;
import dev.prospectos.api.dto.LeadSearchResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lead discovery API for textual discovery use cases.
 */
@RestController
@RequestMapping("/api/leads")
public class LeadDiscoveryController {

    private final LeadDiscoveryService leadDiscoveryService;

    public LeadDiscoveryController(LeadDiscoveryService leadDiscoveryService) {
        this.leadDiscoveryService = leadDiscoveryService;
    }

    @PostMapping("/discover")
    public ResponseEntity<LeadSearchResponse> discover(@Valid @RequestBody LeadDiscoveryRequest request) {
        LeadSearchResponse response = leadDiscoveryService.discoverLeads(request);
        return ResponseEntity.ok(response);
    }
}
