package dev.prospectos.api.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for creating a company.
 */
public record CompanyCreateRequest(
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
