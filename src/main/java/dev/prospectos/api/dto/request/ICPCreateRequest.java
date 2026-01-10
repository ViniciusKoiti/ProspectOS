package dev.prospectos.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Request payload for creating an ICP.
 */
public record ICPCreateRequest(
    @NotBlank(message = "Name is required")
    String name,
    String description,
    List<String> industries,
    List<String> regions,
    List<String> targetRoles,
    String interestTheme
) {
}
