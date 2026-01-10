package dev.prospectos.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Request payload for updating an ICP.
 */
public record ICPUpdateRequest(
    @NotBlank(message = "Name is required")
    String name,
    String description,
    List<String> industries,
    List<String> regions,
    List<String> targetRoles,
    String interestTheme
) {
}
