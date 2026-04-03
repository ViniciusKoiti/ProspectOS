package dev.prospectos.infrastructure.service.prospect;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

record HunterDomainSearchResponse(
    HunterDomainSearchData data
) {
}

record HunterDomainSearchData(
    List<HunterEmailEntry> emails
) {
}

record HunterEmailEntry(
    String value,
    @JsonProperty("first_name")
    String firstName,
    @JsonProperty("last_name")
    String lastName,
    String position,
    Integer confidence
) {
}
