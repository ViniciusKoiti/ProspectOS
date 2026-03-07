package dev.prospectos.ai.factory;

import dev.prospectos.ai.client.LLMClient;
import dev.prospectos.ai.client.LLMProvider;
import dev.prospectos.ai.client.impl.MockLLMClient;
import dev.prospectos.ai.config.AIProviderActivationProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LLMClientFactoryTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient scoringChatClient;

    @Mock
    private ChatClient groqChatClient;

    @Mock
    private ChatClient groqScoringChatClient;

    @Test
    void createClientReturnsProviderAwareMockClientInTestEnvironment() {
        LLMClientFactory factory = factory(
            new MockEnvironment().withProperty("spring.profiles.active", "test"),
            List.of(LLMProvider.GROQ, LLMProvider.OPENAI)
        );

        LLMClient openAi = factory.createClient(LLMProvider.OPENAI);
        LLMClient groq = factory.createClient(LLMProvider.GROQ);
        LLMClient mock = factory.createClient(LLMProvider.MOCK);

        assertThat(openAi).isInstanceOf(MockLLMClient.class);
        assertThat(openAi.getProvider()).isEqualTo(LLMProvider.OPENAI);
        assertThat(groq.getProvider()).isEqualTo(LLMProvider.GROQ);
        assertThat(mock.getProvider()).isEqualTo(LLMProvider.MOCK);
    }

    @Test
    void createPrimaryAndScoringClientReturnMockInTestEnvironment() {
        LLMClientFactory factory = factory(
            new MockEnvironment().withProperty("spring.profiles.active", "test"),
            List.of(LLMProvider.OPENAI)
        );

        assertThat(factory.createPrimaryClient().getProvider()).isEqualTo(LLMProvider.MOCK);
        assertThat(factory.createScoringClient().getProvider()).isEqualTo(LLMProvider.MOCK);
    }

    @Test
    void createBestAvailableClientPrefersFirstActiveAvailableProviderOutsideTestEnvironment() {
        LLMClientFactory factory = factory(new MockEnvironment(), List.of(LLMProvider.GROQ, LLMProvider.OPENAI));
        ReflectionTestUtils.setField(factory, "groqKey", "gsk-live-key");
        ReflectionTestUtils.setField(factory, "openaiKey", "sk-live-key");

        LLMClient client = FactoryCallSupport.bestAvailableClient(factory);

        assertThat(client.getProvider()).isEqualTo(LLMProvider.GROQ);
        assertThat(client.isAvailable()).isTrue();
    }

    @Test
    void createBestAvailableClientFallsBackToOpenAiWhenGroqIsInvalid() {
        LLMClientFactory factory = factory(new MockEnvironment(), List.of(LLMProvider.GROQ, LLMProvider.OPENAI));
        ReflectionTestUtils.setField(factory, "groqKey", "mock-key");
        ReflectionTestUtils.setField(factory, "openaiKey", "sk-live-key");

        LLMClient client = FactoryCallSupport.bestAvailableClient(factory);

        assertThat(client.getProvider()).isEqualTo(LLMProvider.OPENAI);
        assertThat(client.isAvailable()).isTrue();
    }

    @Test
    void createBestAvailableScoringClientUsesGroqScoringClientWhenConfigured() {
        LLMClientFactory factory = factory(new MockEnvironment(), List.of(LLMProvider.GROQ, LLMProvider.OPENAI));
        ReflectionTestUtils.setField(factory, "groqKey", "gsk-live-key");

        LLMClient client = FactoryCallSupport.invoke(factory::createScoringClient);

        assertThat(client.getProvider()).isEqualTo(LLMProvider.GROQ);
        assertThat(client.isAvailable()).isTrue();
    }

    @Test
    void createBestAvailableClientFallsBackToMockWhenNoProviderIsConfigured() {
        LLMClientFactory factory = factory(new MockEnvironment(), List.of(LLMProvider.OPENAI, LLMProvider.GROQ));

        LLMClient client = FactoryCallSupport.bestAvailableClient(factory);

        assertThat(client).isInstanceOf(MockLLMClient.class);
        assertThat(client.getProvider()).isEqualTo(LLMProvider.MOCK);
    }

    @Test
    void createClientReturnsUnavailableSpringAiClientWhenProviderHasNoChatClient() {
        LLMClientFactory factory = new LLMClientFactory(
            provider(null),
            provider(null),
            provider(null),
            provider(null),
            new MockEnvironment(),
            new AIProviderActivationProperties("openai")
        );
        ReflectionTestUtils.setField(factory, "openaiKey", "sk-live-key");

        LLMClient client = FactoryCallSupport.createClient(factory, LLMProvider.OPENAI);

        assertThat(client.getProvider()).isEqualTo(LLMProvider.OPENAI);
        assertThat(client.isAvailable()).isFalse();
    }

    private LLMClientFactory factory(Environment environment, List<LLMProvider> providers) {
        LLMClientFactory factory = new LLMClientFactory(
            provider(chatClient),
            provider(scoringChatClient),
            provider(groqChatClient),
            provider(groqScoringChatClient),
            environment,
            new AIProviderActivationProperties(joinProviders(providers))
        );
        ReflectionTestUtils.setField(factory, "openaiKey", "");
        ReflectionTestUtils.setField(factory, "anthropicKey", "");
        ReflectionTestUtils.setField(factory, "groqKey", "");
        return factory;
    }

    private ObjectProvider<ChatClient> provider(ChatClient value) {
        @SuppressWarnings("unchecked")
        ObjectProvider<ChatClient> provider = mock(ObjectProvider.class);
        lenient().when(provider.getIfAvailable()).thenReturn(value);
        return provider;
    }

    private String joinProviders(List<LLMProvider> providers) {
        return providers.stream()
            .map(provider -> provider.name().toLowerCase())
            .reduce((left, right) -> left + "," + right)
            .orElse("mock");
    }
}
