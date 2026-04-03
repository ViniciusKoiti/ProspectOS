package dev.prospectos.infrastructure.config;

import dev.prospectos.api.dto.ProspectEnrichRequest;
import dev.prospectos.api.dto.ProspectEnrichResponse;
import dev.prospectos.api.dto.ProspectWebsiteAuditResponse;
import dev.prospectos.infrastructure.service.prospect.ProspectEnrichmentFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test configuration that provides mock ProspectEnrichmentFacade for enrichment tests.
 */
@Configuration
@Profile("test")
public class TestProspectConfiguration {

    @Bean
    @Primary
    public ProspectEnrichmentFacade prospectEnrichmentFacade() {
        ProspectEnrichmentFacade mock = mock(ProspectEnrichmentFacade.class);

        when(mock.enrich(any(ProspectEnrichRequest.class))).thenAnswer(invocation -> {
            ProspectEnrichRequest request = invocation.getArgument(0);

            if (request.name() == null || request.name().trim().isEmpty() ||
                request.website() == null || request.website().trim().isEmpty()) {
                throw new IllegalArgumentException("Name and website are required");
            }

            return new ProspectEnrichResponse(
                "Example Company",
                request.website(),
                request.industry() != null ? request.industry() : "Technology",
                "Comprehensive AI-generated analysis of the company's market position, competitive advantages, and growth opportunities in the technology sector.",
                new ProspectWebsiteAuditResponse(
                    72,
                    "REVIEW",
                    request.website().startsWith("https://"),
                    true,
                    true,
                    false,
                    null,
                    java.util.List.of("Internal audit identified partial website quality signals.")
                )
            );
        });

        return mock;
    }
}
