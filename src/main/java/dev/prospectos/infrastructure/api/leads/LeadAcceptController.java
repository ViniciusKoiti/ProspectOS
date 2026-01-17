package dev.prospectos.infrastructure.api.leads;

import dev.prospectos.api.dto.request.AcceptLeadRequest;
import dev.prospectos.api.dto.response.AcceptLeadResponse;
import dev.prospectos.infrastructure.service.leads.LeadAcceptService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API controller for accepting leads from preview.
 */
@RestController
@RequestMapping("/api/leads")
public class LeadAcceptController {

    private final LeadAcceptService leadAcceptService;

    public LeadAcceptController(LeadAcceptService leadAcceptService) {
        this.leadAcceptService = leadAcceptService;
    }

    /**
     * POST /api/leads/accept
     *
     * Accepts a lead from preview and persists it.
     * The request must include the leadKey from the preview response for idempotency.
     *
     * Example request:
     * {
     *   "leadKey": "abc123...",
     *   "candidate": { "name": "TechCorp", "website": "https://techcorp.com", ... },
     *   "score": { "value": 85, "category": "HOT", "reasoning": "Great fit" },
     *   "source": { "name": "apollo", "url": "https://techcorp.com", "recordedAt": "2026-01-17T..." }
     * }
     *
     * @param request the accept lead request
     * @return the persisted company data
     */
    @PostMapping("/accept")
    public ResponseEntity<AcceptLeadResponse> acceptLead(@Valid @RequestBody AcceptLeadRequest request) {
        AcceptLeadResponse response = leadAcceptService.acceptLead(request);
        return ResponseEntity.ok(response);
    }
}
