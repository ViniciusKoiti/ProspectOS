package dev.prospectos.infrastructure.service.discovery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record GooglePlacesSearchTextResponse(List<GooglePlace> places) {
}

@JsonIgnoreProperties(ignoreUnknown = true)
record GooglePlace(
    GoogleDisplayName displayName,
    String formattedAddress,
    String websiteUri,
    String nationalPhoneNumber,
    List<String> types
) {
}

@JsonIgnoreProperties(ignoreUnknown = true)
record GoogleDisplayName(String text) {
}
