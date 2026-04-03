package dev.prospectos.infrastructure.service.prospect;

record GooglePageSpeedResponse(
    GoogleLighthouseResult lighthouseResult
) {
}

record GoogleLighthouseResult(
    GooglePageSpeedCategories categories
) {
}

record GooglePageSpeedCategories(
    GooglePageSpeedCategory performance
) {
}

record GooglePageSpeedCategory(
    Double score
) {
}
