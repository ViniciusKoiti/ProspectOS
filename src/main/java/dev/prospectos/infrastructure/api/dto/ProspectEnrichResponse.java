package dev.prospectos.infrastructure.api.dto;

public record ProspectEnrichResponse(
    String name,
    String website,
    String industry,
    String analysis
) {}
