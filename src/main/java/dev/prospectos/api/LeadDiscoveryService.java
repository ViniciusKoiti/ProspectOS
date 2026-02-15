package dev.prospectos.api;

import dev.prospectos.api.dto.LeadDiscoveryRequest;
import dev.prospectos.api.dto.LeadSearchResponse;

/**
 * Public interface for textual lead discovery.
 */
public interface LeadDiscoveryService {

    LeadSearchResponse discoverLeads(LeadDiscoveryRequest request);
}
