package dev.prospectos.api;

import dev.prospectos.api.dto.LeadSearchRequest;
import dev.prospectos.api.dto.LeadSearchResponse;

/**
 * Public interface for on-demand lead search.
 */
public interface LeadSearchService {

    LeadSearchResponse searchLeads(LeadSearchRequest request);
}
