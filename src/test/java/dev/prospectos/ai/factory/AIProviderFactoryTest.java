package dev.prospectos.ai.factory;

import dev.prospectos.ai.client.AIProvider;
import dev.prospectos.ai.client.LLMClient;
import dev.prospectos.ai.client.LLMProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIProviderFactoryTest {

    @Mock
    private LLMClientFactory llmClientFactory;

    @Mock
    private LLMClient primaryClient;

    @Mock
    private LLMClient scoringClient;

    @Mock
    private LLMClient mockClient;

    private AIProviderFactory factory;

    @BeforeEach
    void setUp() {
        factory = new AIProviderFactory(llmClientFactory);
    }

    @Test
    void createPrimaryProviderUsesPrimaryAndScoringClients() {
        when(primaryClient.getProvider()).thenReturn(LLMProvider.OPENAI);
        when(scoringClient.getProvider()).thenReturn(LLMProvider.GROQ);
        when(scoringClient.queryWithFunctions("Prompt", "scrapeWebsite", "searchCompanyNews")).thenReturn("enriched");
        when(llmClientFactory.createBestAvailableClient()).thenReturn(primaryClient);
        when(llmClientFactory.createScoringClient()).thenReturn(scoringClient);

        AIProvider provider = factory.createPrimaryProvider();

        assertThat(provider.getClient()).isSameAs(primaryClient);
        assertThat(provider.enrichCompanyData("Prompt")).isEqualTo("enriched");
        verify(llmClientFactory).createBestAvailableClient();
        verify(llmClientFactory).createScoringClient();
    }

    @Test
    void createProviderUsesRequestedProviderClient() {
        when(llmClientFactory.createClient(LLMProvider.ANTHROPIC)).thenReturn(mockClient);

        AIProvider provider = factory.createProvider(LLMProvider.ANTHROPIC);

        assertThat(provider.getClient()).isSameAs(mockClient);
        verify(llmClientFactory).createClient(LLMProvider.ANTHROPIC);
    }

    @Test
    void createScoringProviderUsesScoringClient() {
        when(scoringClient.getProvider()).thenReturn(LLMProvider.GROQ);
        when(llmClientFactory.createScoringClient()).thenReturn(scoringClient);

        AIProvider provider = factory.createScoringProvider();

        assertThat(provider.getClient()).isSameAs(scoringClient);
        verify(llmClientFactory).createScoringClient();
    }

    @Test
    void createMockProviderUsesMockClient() {
        when(llmClientFactory.createClient(LLMProvider.MOCK)).thenReturn(mockClient);

        AIProvider provider = factory.createMockProvider();

        assertThat(provider.getClient()).isSameAs(mockClient);
        verify(llmClientFactory).createClient(LLMProvider.MOCK);
    }
}
