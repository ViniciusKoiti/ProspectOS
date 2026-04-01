package dev.prospectos.infrastructure.service.discovery;

import java.util.List;
import java.util.Locale;

final class GooglePlacesResponseMapper {

    private final String sourceName;

    GooglePlacesResponseMapper(String sourceName) {
        this.sourceName = sourceName;
    }

    List<DiscoveredLeadCandidate> toCandidates(List<GooglePlace> places, int limit) {
        if (places == null || places.isEmpty()) {
            return List.of();
        }
        return places.stream().limit(limit).map(this::toCandidate).toList();
    }

    private DiscoveredLeadCandidate toCandidate(GooglePlace place) {
        String name = place.displayName() == null ? null : place.displayName().text();
        String industry = place.types() == null || place.types().isEmpty() ? "local business" : normalize(place.types().getFirst());
        List<String> contacts = place.nationalPhoneNumber() == null || place.nationalPhoneNumber().isBlank()
            ? List.of()
            : List.of(place.nationalPhoneNumber().trim());
        return new DiscoveredLeadCandidate(name, place.websiteUri(), industry, place.formattedAddress(), place.formattedAddress(), contacts, sourceName);
    }

    private String normalize(String value) {
        return value == null ? "local business" : value.replace('_', ' ').trim().toLowerCase(Locale.ROOT);
    }
}
