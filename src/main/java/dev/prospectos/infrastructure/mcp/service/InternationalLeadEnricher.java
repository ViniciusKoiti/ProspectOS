package dev.prospectos.infrastructure.mcp.service;

import dev.prospectos.api.mcp.CompanyIntelligence;
import dev.prospectos.api.mcp.ContactData;
import dev.prospectos.api.mcp.EnrichedLeadData;
import dev.prospectos.api.mcp.LeadData;
import dev.prospectos.api.mcp.TechnologyStack;

import java.util.List;
import java.util.Map;
import java.util.Random;

final class InternationalLeadEnricher {

    private final Random random;

    InternationalLeadEnricher(Random random) {
        this.random = random;
    }

    EnrichedLeadData enrich(LeadData leadData, List<String> sources) {
        var provider = List.of("AWS", "Azure", "Google Cloud", "Heroku").get(random.nextInt(4));
        var intelligence = new CompanyIntelligence(
            10 + random.nextInt(500),
            "$" + (1 + random.nextInt(100)) + "M",
            List.of("Bootstrap", "Seed", "Series A", "Series B", "Growth").get(random.nextInt(5)),
            List.of("CEO John Smith", "CTO Maria Garcia", "VP Sales Robert Johnson"),
            Map.of("linkedinFollowers", 500 + random.nextInt(5000), "twitterFollowers", 100 + random.nextInt(2000), "glassdoorRating", 3.5 + (random.nextDouble() * 1.5))
        );
        var contacts = List.of(
            new ContactData("John Smith", "CEO", "john.smith@" + domain(leadData.website()), "linkedin.com/in/johnsmith", 0.85 + random.nextDouble() * 0.1),
            new ContactData("Maria Garcia", "CTO", "maria.garcia@" + domain(leadData.website()), "linkedin.com/in/mariagarcia", 0.80 + random.nextDouble() * 0.15)
        );
        var techStack = new TechnologyStack(List.of("React", "Angular", "Vue.js", "Spring Boot", "Django").subList(0, 1 + random.nextInt(2)), List.of(provider), List.of("Docker", "Kubernetes", "Jenkins", "Terraform").subList(0, 1 + random.nextInt(3)), provider);
        return new EnrichedLeadData(leadData, intelligence, contacts, techStack, Math.min(0.95, 0.6 + (sources.size() * 0.05) + (random.nextDouble() * 0.2)));
    }

    private String domain(String website) {
        return website.replace("https://www.", "").replace("http://", "");
    }
}
