package dev.prospectos.infrastructure.api.dto;

public record ProspectEnrichRequest(
    String name,
    String website,
    String industry
) {}
