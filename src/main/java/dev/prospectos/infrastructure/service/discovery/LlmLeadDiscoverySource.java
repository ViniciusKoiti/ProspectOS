package dev.prospectos.infrastructure.service.discovery;

import dev.prospectos.ai.client.AIProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * LLM-first textual discovery strategy.
 */
@Component
@Profile("!test")
public class LlmLeadDiscoverySource implements LeadDiscoverySource {

    private static final String SOURCE_NAME = "llm-discovery";

    private final AIProvider aiProvider;
    private final LlmDiscoveryResponseConverter converter;

    public LlmLeadDiscoverySource(AIProvider aiProvider, LlmDiscoveryResponseConverter converter) {
        this.aiProvider = aiProvider;
        this.converter = converter;
    }

    @Override
    public String sourceName() {
        return SOURCE_NAME;
    }

    @Override
    public List<DiscoveredLeadCandidate> discover(DiscoveryContext context) {
        String prompt = """
            You are a B2B lead discovery engine.

            Query: %s
            Role: %s
            Max candidates: %d

            Return only valid JSON with this exact shape:
            {
              "candidates": [
                {
                  "name": "string",
                  "website": "https://example.com",
                  "industry": "string",
                  "description": "string",
                  "location": "string",
                  "contacts": ["email@example.com"]
                }
              ]
            }

            Rules:
            - No markdown
            - No comments
            - No references or explanations
            - Include only candidates likely relevant to the query
            - Include at most the requested number of candidates
            """.formatted(context.query(), context.role() == null ? "UNKNOWN" : context.role(), context.limit());

        String raw = aiProvider.getClient().query(prompt);
        return converter.convert(raw, SOURCE_NAME);
    }
}
