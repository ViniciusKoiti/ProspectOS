package dev.prospectos.infrastructure.service.prospect;

import java.util.List;

import dev.prospectos.api.dto.ProspectContactResponse;

public interface HunterContactEnrichmentProvider {
    List<ProspectContactResponse> findContacts(String website);
}
