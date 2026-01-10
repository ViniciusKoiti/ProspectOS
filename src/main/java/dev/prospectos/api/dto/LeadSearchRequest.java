package dev.prospectos.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for on-demand lead search.
 */
public record LeadSearchRequest(
    @NotBlank(message = "Query cannot be blank")
    String query,
    @Min(value = 1, message = "Limit must be between 1 and 100")
    @Max(value = 100, message = "Limit must be between 1 and 100")
    Integer limit,
    List<String> sources,
    UUID icpId
) {
}
