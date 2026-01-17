package dev.prospectos.api.dto.response;

import dev.prospectos.api.dto.CompanyDTO;

/**
 * Response from accepting a lead.
 * Contains the persisted company data.
 */
public record AcceptLeadResponse(
    CompanyDTO company,
    String message
) {
}
