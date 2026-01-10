package dev.prospectos.api.dto;

public record ProspectEnrichRequest(
    String name,
    String website,
    String industry
) {}
