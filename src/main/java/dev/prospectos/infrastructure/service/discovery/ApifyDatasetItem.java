package dev.prospectos.infrastructure.service.discovery;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
record ApifyDatasetItem(
    String title,
    String name,
    String address,
    String fullAddress,
    String website,
    String phone,
    String phoneUnformatted,
    String categoryName,
    String category,
    List<String> categories
) {
}
