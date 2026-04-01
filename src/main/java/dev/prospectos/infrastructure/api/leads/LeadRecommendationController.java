package dev.prospectos.infrastructure.api.leads;

import dev.prospectos.api.LeadRecommendationService;
import dev.prospectos.api.dto.LeadRecommendationRequest;
import dev.prospectos.api.dto.LeadRecommendationResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leads")
public class LeadRecommendationController {

    private final LeadRecommendationService leadRecommendationService;

    public LeadRecommendationController(LeadRecommendationService leadRecommendationService) {
        this.leadRecommendationService = leadRecommendationService;
    }

    @PostMapping("/recommendation")
    public ResponseEntity<LeadRecommendationResponse> recommend(@Valid @RequestBody LeadRecommendationRequest request) {
        return ResponseEntity.ok(leadRecommendationService.recommend(request));
    }
}
