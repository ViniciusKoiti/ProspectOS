package dev.prospectos.infrastructure.service.discovery;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Test-safe in-memory discovery source that keeps the main lead discovery flow
 * available without requiring real AI providers.
 */
@Component
@Profile("test")
public class InMemoryLeadDiscoverySource implements LeadDiscoverySource {

    private static final String SOURCE_NAME = "llm-discovery";

    @Override
    public String sourceName() {
        return SOURCE_NAME;
    }

    @Override
    public List<DiscoveredLeadCandidate> discover(DiscoveryContext context) {
        if (context.limit() <= 0) {
            return List.of();
        }

        return List.of(new DiscoveredLeadCandidate(
            "Acme Foods",
            "https://acmefoods.com",
            "Food Distribution",
            "Regional food supplier",
            "Maringa, PR",
            List.of("contact@acmefoods.com"),
            SOURCE_NAME
        ));
    }
}
