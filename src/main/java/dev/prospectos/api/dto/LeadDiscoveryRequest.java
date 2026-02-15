package dev.prospectos.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Request DTO for textual lead discovery.
 */
public record LeadDiscoveryRequest(
    @NotBlank(message = "Query cannot be blank")
    String query,
    String role,
    @Min(value = 1, message = "Limit must be between 1 and 100")
    @Max(value = 100, message = "Limit must be between 1 and 100")
    Integer limit,
    List<String> sources,
    Long icpId
) {
}
