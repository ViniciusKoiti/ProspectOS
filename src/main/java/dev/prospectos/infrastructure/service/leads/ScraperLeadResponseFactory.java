package dev.prospectos.infrastructure.service.leads;

import java.util.List;
import java.util.UUID;

import dev.prospectos.api.dto.LeadResultDTO;
import dev.prospectos.api.dto.LeadSearchResponse;
import dev.prospectos.api.dto.LeadSearchStatus;

final class ScraperLeadResponseFactory {

    LeadSearchResponse failed(String error) {
        return new LeadSearchResponse(
            LeadSearchStatus.FAILED,
            List.of(),
            UUID.randomUUID(),
            error == null ? "Scraper failed" : error
        );
    }

    LeadSearchResponse noLeads() {
        return new LeadSearchResponse(
            LeadSearchStatus.COMPLETED,
            List.of(),
            UUID.randomUUID(),
            "No leads found"
        );
    }

    LeadSearchResponse completed(List<LeadResultDTO> leads, String message) {
        return new LeadSearchResponse(
            LeadSearchStatus.COMPLETED,
            leads == null ? List.of() : leads,
            UUID.randomUUID(),
            message == null ? "Search completed" : message
        );
    }
}
