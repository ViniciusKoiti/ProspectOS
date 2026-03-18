package dev.prospectos.infrastructure.service.discovery;

import dev.prospectos.ai.client.LlmStructuredResponseSanitizer;
import dev.prospectos.api.dto.CompanyCandidateDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LlmDiscoveryResponseConverterTest {

    private final LlmDiscoveryResponseConverter converter =
        new LlmDiscoveryResponseConverter(new LlmStructuredResponseSanitizer());

    @Test
    void convert_ParsesValidTypedSchema() {
        String raw = """
            {
              "candidates": [
                {
                  "name": "Acme Foods",
                  "website": "acmefoods.com",
                  "industry": "Food Distribution",
                  "description": "Regional supplier",
                  "location": "Maringa, PR",
                  "contacts": ["contact@acmefoods.com"]
                }
              ]
            }
            """;

        List<DiscoveredLeadCandidate> result = converter.convert(raw, "llm-discovery");

        assertEquals(1, result.size());
        assertEquals("Acme Foods", result.getFirst().name());
        assertEquals("https://acmefoods.com", result.getFirst().website());
    }

    @Test
    void convert_ThrowsWhenResponseIsMalformedJson() {
        String raw = "{\"candidates\": [ { \"name\": \"Acme\" ";

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> converter.convert(raw, "llm-discovery")
        );

        assertTrue(exception.getMessage() != null && !exception.getMessage().isBlank());
        assertEquals(1, converter.parseFailureCount("llm-discovery"));
    }

    @Test
    void convert_ThrowsWhenSchemaDoesNotContainCandidates() {
        String raw = """
            {
              "items": [
                {
                  "name": "Acme Foods",
                  "website": "https://acmefoods.com"
                }
              ]
            }
            """;

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> converter.convert(raw, "llm-discovery")
        );

        assertTrue(exception.getMessage() != null && !exception.getMessage().isBlank());
        assertEquals(1, converter.parseFailureCount("llm-discovery"));
    }

    @Test
    void convert_ReturnsEmptyListForBlankResponse() {
        List<DiscoveredLeadCandidate> result = converter.convert("   ", "llm-discovery");

        assertTrue(result.isEmpty());
        assertEquals(0, converter.parseFailureCount("llm-discovery"));
    }

    @Test
    void convert_PreservesCandidatesWithoutWebsite() {
        String raw = """
            {
              "candidates": [
                {
                  "name": "Acme Foods",
                  "industry": "Food Distribution",
                  "description": "Regional supplier",
                  "location": "Maringa, PR",
                  "contacts": ["contact@acmefoods.com"]
                }
              ]
            }
            """;

        List<DiscoveredLeadCandidate> result = converter.convert(raw, "llm-discovery");

        assertEquals(1, result.size());
        assertEquals("Acme Foods", result.getFirst().name());
        assertEquals(null, result.getFirst().website());
        assertEquals(CompanyCandidateDTO.WebsitePresence.NO_WEBSITE, result.getFirst().websitePresence());
    }
}
