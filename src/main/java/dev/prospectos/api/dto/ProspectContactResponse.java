package dev.prospectos.api.dto;

public record ProspectContactResponse(
    String email,
    String name,
    String position,
    Integer confidence,
    String source
) {
}
