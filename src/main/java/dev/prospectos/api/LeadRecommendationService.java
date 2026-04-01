package dev.prospectos.api;

import dev.prospectos.api.dto.LeadRecommendationRequest;
import dev.prospectos.api.dto.LeadRecommendationResponse;

public interface LeadRecommendationService {

    LeadRecommendationResponse recommend(LeadRecommendationRequest request);
}
