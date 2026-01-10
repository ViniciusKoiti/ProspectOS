package dev.prospectos.api.dto;

public record ProspectEnrichResponse(
    String name,
    String website,
    String industry,
    String analysis
) {}
