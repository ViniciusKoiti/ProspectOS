package dev.prospectos.infrastructure.mcp.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

final class InternationalLeadFixtures {

    private final Random random;

    InternationalLeadFixtures(Random random) {
        this.random = random;
    }

    String companyName(String country) {
        var prefixes = List.of("Innovative", "Global", "Smart", "Advanced", "Digital", "Pro");
        var suffixes = List.of("Solutions", "Tech", "Systems", "Group", "International", "Labs");
        var countryCode = country.substring(0, Math.min(3, country.length())).toUpperCase();
        return prefixes.get(random.nextInt(prefixes.size())) + " " + countryCode + " " + suffixes.get(random.nextInt(suffixes.size()));
    }

    String city(String country) {
        var cities = Map.of(
            "brazil", List.of("Sao Paulo", "Rio de Janeiro", "Brasilia", "Salvador"),
            "argentina", List.of("Buenos Aires", "Cordoba", "Rosario", "Mendoza"),
            "chile", List.of("Santiago", "Valparaiso", "Concepcion", "Antofagasta"),
            "mexico", List.of("Mexico City", "Guadalajara", "Monterrey", "Puebla"),
            "spain", List.of("Madrid", "Barcelona", "Valencia", "Seville"),
            "italy", List.of("Rome", "Milan", "Naples", "Turin")
        );
        var options = cities.getOrDefault(country.toLowerCase(), List.of("Capital City"));
        return options.get(random.nextInt(options.size()));
    }

    Map<String, Object> additionalData(String industry) {
        var data = new HashMap<String, Object>();
        data.put("employees", 10 + random.nextInt(500));
        data.put("foundedYear", 1995 + random.nextInt(28));
        data.put("revenue", "$" + (1 + random.nextInt(50)) + "M");
        data.put("specialties", specialties(industry));
        return data;
    }

    private List<String> specialties(String industry) {
        var specialties = Map.of(
            "technology", List.of("Software Development", "Cloud Computing", "AI/ML", "Cybersecurity"),
            "finance", List.of("Investment Banking", "Financial Planning", "Insurance", "Fintech"),
            "healthcare", List.of("Medical Devices", "Pharmaceuticals", "Digital Health", "Telemedicine"),
            "manufacturing", List.of("Industrial Automation", "Quality Control", "Supply Chain", "IoT"),
            "retail", List.of("E-commerce", "Customer Analytics", "Inventory Management", "Omnichannel"),
            "consulting", List.of("Strategy Consulting", "Digital Transformation", "Change Management", "Analytics")
        ).getOrDefault(industry.toLowerCase(), List.of("General Business"));
        return specialties.subList(0, Math.min(2 + random.nextInt(2), specialties.size()));
    }
}
