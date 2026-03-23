package dev.prospectos.infrastructure.service.discovery;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

record AmazonLocationSearchTextRequest(
    @JsonProperty("QueryText") String queryText,
    @JsonProperty("Language") String language,
    @JsonProperty("IntendedUse") String intendedUse,
    @JsonProperty("MaxResults") Integer maxResults,
    @JsonProperty("Filter") AmazonLocationSearchTextFilter filter
) {
}

record AmazonLocationSearchTextFilter(
    @JsonProperty("IncludeCountries") List<String> includeCountries
) {
}

@JsonIgnoreProperties(ignoreUnknown = true)
record AmazonLocationSearchTextResponse(
    @JsonProperty("ResultItems") List<AmazonLocationResultItem> resultItems
) {
}

@JsonIgnoreProperties(ignoreUnknown = true)
record AmazonLocationResultItem(
    @JsonProperty("Title") String title,
    @JsonProperty("PlaceType") String placeType,
    @JsonProperty("Address") AmazonLocationAddress address,
    @JsonProperty("Categories") List<AmazonLocationCategory> categories,
    @JsonProperty("Contacts") AmazonLocationContacts contacts
) {
}

@JsonIgnoreProperties(ignoreUnknown = true)
record AmazonLocationAddress(
    @JsonProperty("Label") String label,
    @JsonProperty("Locality") String locality,
    @JsonProperty("District") String district,
    @JsonProperty("SubRegion") AmazonLocationSubRegion subRegion,
    @JsonProperty("Country") AmazonLocationCountry country
) {
}

@JsonIgnoreProperties(ignoreUnknown = true)
record AmazonLocationSubRegion(
    @JsonProperty("Name") String name
) {
}

@JsonIgnoreProperties(ignoreUnknown = true)
record AmazonLocationCountry(
    @JsonProperty("Code2") String code2,
    @JsonProperty("Code3") String code3,
    @JsonProperty("Name") String name
) {
}

@JsonIgnoreProperties(ignoreUnknown = true)
record AmazonLocationCategory(
    @JsonProperty("Name") String name
) {
}

@JsonIgnoreProperties(ignoreUnknown = true)
record AmazonLocationContacts(
    @JsonProperty("Emails") List<AmazonLocationContactValue> emails,
    @JsonProperty("Websites") List<AmazonLocationContactValue> websites
) {
}

@JsonIgnoreProperties(ignoreUnknown = true)
record AmazonLocationContactValue(
    @JsonProperty("Value") String value
) {
}
