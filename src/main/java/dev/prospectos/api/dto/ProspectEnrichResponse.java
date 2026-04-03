package dev.prospectos.api.dto;

import java.util.List;

public record ProspectEnrichResponse(
    String name,
    String website,
    String industry,
    String analysis,
    ProspectWebsiteAuditResponse audit,
    List<ProspectContactResponse> contacts
) {
}
