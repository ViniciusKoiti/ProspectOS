package dev.prospectos.web;

import dev.prospectos.api.LeadSearchService;
import dev.prospectos.api.dto.LeadSearchRequest;
import dev.prospectos.api.dto.LeadSearchResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

/**
 * Lead search API for the on-demand flow.
 */
@RestController
@RequestMapping("/api/leads")
public class LeadSearchController {

    private final LeadSearchService leadSearchService;

    public LeadSearchController(LeadSearchService leadSearchService) {
        this.leadSearchService = leadSearchService;
    }

    /**
     * POST /api/leads/search
     *
     * Example request:
     * { "query": "fazendeiros PR", "limit": 10, "sources": ["in-memory"] }
     */
    @PostMapping("/search")
    public ResponseEntity<LeadSearchResponse> search(@Valid @RequestBody LeadSearchRequest request) {
        LeadSearchResponse response = leadSearchService.searchLeads(request);
        return ResponseEntity.ok(response);
    }
}
