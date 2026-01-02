package dev.prospectos.api.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for updating a company.
 */
public record CompanyUpdateRequest(
    @NotBlank(message = "Name is required")
    String name,
    @NotBlank(message = "Industry is required")
    String industry,
    @NotBlank(message = "Website is required")
    String website,
    String description,
    String country,
    String city,
    String size
) {
}
