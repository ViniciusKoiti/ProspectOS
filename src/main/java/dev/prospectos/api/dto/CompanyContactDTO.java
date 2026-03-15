package dev.prospectos.api.dto;

/**
 * Contact data exposed for a persisted company.
 */
public record CompanyContactDTO(
    String name,
    String email,
    String position,
    String phoneNumber
) {
}
