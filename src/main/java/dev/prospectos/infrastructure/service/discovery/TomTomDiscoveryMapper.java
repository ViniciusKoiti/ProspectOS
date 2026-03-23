package dev.prospectos.infrastructure.service.discovery;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class TomTomDiscoveryMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DiscoveryCandidateMapper candidateMapper = new DiscoveryCandidateMapper();

    List<DiscoveredLeadCandidate> map(String payload, int limit, String sourceName) {
        if (payload == null || payload.isBlank() || limit <= 0) {
            return List.of();
        }

        try {
            JsonNode results = objectMapper.readTree(payload).path("results");
            if (!results.isArray()) {
                return List.of();
            }

            List<DiscoveredLeadCandidate> mapped = new ArrayList<>();
            for (JsonNode result : results) {
                DiscoveredLeadCandidate candidate = toCandidate(result, sourceName);
                if (candidate != null) {
                    mapped.add(candidate);
                }
                if (mapped.size() >= limit) {
                    break;
                }
            }
            return mapped;
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    private DiscoveredLeadCandidate toCandidate(JsonNode result, String sourceName) {
        JsonNode poi = result.path("poi");
        JsonNode address = result.path("address");
        JsonNode position = result.path("position");

        String name = firstNonBlank(text(poi, "name"), text(address, "freeformAddress"));
        String website = text(poi, "url");
        String industry = firstNonBlank(classification(poi), firstArrayText(poi.path("categories")));
        String description = buildDescription(industry);
        String location = buildLocation(address, position);
        List<String> contacts = contacts(poi);

        return candidateMapper.toCandidate(sourceName, name, website, industry, description, location, contacts);
    }

    private String classification(JsonNode poi) {
        JsonNode names = poi.path("classifications").path(0).path("names");
        if (names.isArray() && !names.isEmpty()) {
            return text(names.get(0), "name");
        }
        return null;
    }

    private String firstArrayText(JsonNode array) {
        if (array.isArray() && !array.isEmpty()) {
            JsonNode value = array.get(0);
            if (value != null && !value.isNull()) {
                String text = value.asText().trim();
                return text.isBlank() ? null : text;
            }
        }
        return null;
    }

    private List<String> contacts(JsonNode poi) {
        String phone = text(poi, "phone");
        return phone == null ? List.of() : List.of(phone);
    }

    private String buildDescription(String industry) {
        String category = industry == null ? "business" : industry;
        return "TomTom POI result - " + category;
    }

    private String buildLocation(JsonNode address, JsonNode position) {
        String freeform = text(address, "freeformAddress");
        if (freeform != null) {
            return freeform;
        }

        String composed = joinParts(
            text(address, "municipality"),
            text(address, "countrySubdivision"),
            text(address, "country")
        );
        if (composed != null) {
            return composed;
        }

        if (position.hasNonNull("lat") && position.hasNonNull("lon")) {
            return String.format(Locale.ROOT, "%.6f,%.6f", position.get("lat").asDouble(), position.get("lon").asDouble());
        }
        return null;
    }

    private String joinParts(String... values) {
        List<String> parts = new ArrayList<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                parts.add(value);
            }
        }
        return parts.isEmpty() ? null : String.join(", ", parts);
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        String text = value.asText().trim();
        return text.isBlank() ? null : text;
    }

    private String firstNonBlank(String first, String second) {
        return first == null || first.isBlank() ? second : first;
    }
}
