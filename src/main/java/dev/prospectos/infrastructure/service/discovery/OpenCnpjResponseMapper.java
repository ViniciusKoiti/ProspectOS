package dev.prospectos.infrastructure.service.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class OpenCnpjResponseMapper {

    private final String sourceName;

    OpenCnpjResponseMapper(String sourceName) {
        this.sourceName = sourceName;
    }

    List<DiscoveredLeadCandidate> toCandidates(Map<String, Object> payload, int limit) {
        if (payload == null || limit <= 0) {
            return List.of();
        }
        Object rawResults = payload.getOrDefault("results", payload.get("data"));
        if (!(rawResults instanceof List<?> results)) {
            return List.of();
        }
        List<DiscoveredLeadCandidate> mapped = new ArrayList<>();
        for (Object item : results) {
            if (!(item instanceof Map<?, ?> row)) {
                continue;
            }
            DiscoveredLeadCandidate candidate = toCandidate(row);
            if (candidate != null) {
                mapped.add(candidate);
            }
            if (mapped.size() >= limit) {
                break;
            }
        }
        return List.copyOf(mapped);
    }

    private DiscoveredLeadCandidate toCandidate(Map<?, ?> row) {
        String name = firstText(row, "razao_social", "nome_fantasia", "name");
        if (name == null) {
            return null;
        }
        String website = firstText(row, "website", "site", "url");
        String industry = normalizeIndustry(firstText(row, "atividade_principal", "industry", "segmento"));
        String city = firstText(row, "municipio", "cidade");
        String state = firstText(row, "uf", "estado");
        String location = city == null ? state : state == null ? city : city + ", " + state;
        String email = firstText(row, "email");
        String description = firstText(row, "descricao", "description");
        if (description == null) {
            description = "OpenCNPJ candidate";
        }
        List<String> contacts = email == null ? List.of() : List.of(email);
        return new DiscoveredLeadCandidate(name, website, industry, description, location, contacts, sourceName);
    }

    private String normalizeIndustry(String value) {
        if (value == null || value.isBlank()) {
            return "other";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String firstText(Map<?, ?> row, String... keys) {
        for (String key : keys) {
            Object value = row.get(key);
            if (value == null) {
                continue;
            }
            String text = String.valueOf(value).trim();
            if (!text.isBlank()) {
                return text;
            }
        }
        return null;
    }
}
