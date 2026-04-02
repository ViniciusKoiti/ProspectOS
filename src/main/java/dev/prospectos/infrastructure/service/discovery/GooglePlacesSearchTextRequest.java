package dev.prospectos.infrastructure.service.discovery;

record GooglePlacesSearchTextRequest(
    String textQuery,
    String languageCode,
    int maxResultCount
) {
}
