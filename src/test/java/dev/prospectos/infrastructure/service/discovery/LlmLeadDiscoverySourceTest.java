package dev.prospectos.infrastructure.service.discovery;

import dev.prospectos.ai.client.AIProvider;
import dev.prospectos.ai.client.LLMClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LlmLeadDiscoverySourceTest {

    @Mock
    private AIProvider aiProvider;

    @Mock
    private LLMClient llmClient;

    @Mock
    private LlmDiscoveryResponseConverter converter;

    private LlmLeadDiscoverySource source;

    @BeforeEach
    void setUp() {
        lenient().when(aiProvider.getClient()).thenReturn(llmClient);
        source = new LlmLeadDiscoverySource(aiProvider, converter);
    }

    @Test
    void sourceNameReturnsLlmDiscovery() {
        assertThat(source.sourceName()).isEqualTo("llm-discovery");
    }

    @Test
    void discoverBuildsPromptAndDelegatesToConverter() {
        String raw = "{\"candidates\":[]}";
        List<DiscoveredLeadCandidate> converted = List.of(new DiscoveredLeadCandidate(
            "Acme",
            "https://acme.com",
            "technology",
            "desc",
            "Sao Paulo, BR",
            List.of("ceo@acme.com"),
            "llm-discovery"
        ));
        when(llmClient.query(contains("Query: cloud companies"))).thenReturn(raw);
        when(converter.convert(raw, "llm-discovery")).thenReturn(converted);

        List<DiscoveredLeadCandidate> result = source.discover(new DiscoveryContext(
            "cloud companies",
            "CTO",
            3,
            null
        ));

        assertThat(result).isEqualTo(converted);
        verify(llmClient).query(contains("Role: CTO"));
        verify(llmClient).query(contains("Max candidates: 3"));
        verify(converter).convert(raw, "llm-discovery");
    }

    @Test
    void discoverUsesUnknownRoleWhenRoleIsNull() {
        String raw = "{\"candidates\":[]}";
        when(llmClient.query(contains("Role: UNKNOWN"))).thenReturn(raw);
        when(converter.convert(raw, "llm-discovery")).thenReturn(List.of());

        List<DiscoveredLeadCandidate> result = source.discover(new DiscoveryContext(
            "fintech",
            null,
            2,
            null
        ));

        assertThat(result).isEmpty();
        verify(llmClient).query(contains("Query: fintech"));
        verify(llmClient).query(contains("Role: UNKNOWN"));
        verify(converter).convert(raw, "llm-discovery");
    }
}
