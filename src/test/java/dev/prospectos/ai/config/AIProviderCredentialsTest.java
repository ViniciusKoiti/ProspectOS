package dev.prospectos.ai.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AIProviderCredentialsTest {

    @Test
    void reportsValidKeysForConfiguredProviders() {
        AIProviderCredentials credentials = new AIProviderCredentials("sk-live-key", "anth-live-key", "gsk-live-key");

        assertThat(credentials.hasValidOpenAiKey()).isTrue();
        assertThat(credentials.hasValidAnthropicKey()).isTrue();
        assertThat(credentials.hasValidGroqKey()).isTrue();
    }

    @Test
    void rejectsBlankAndMockLikeKeys() {
        AIProviderCredentials blank = new AIProviderCredentials(" ", null, "");
        AIProviderCredentials fake = new AIProviderCredentials("mock-key", "test-key", "sk-test-123");

        assertThat(blank.hasValidOpenAiKey()).isFalse();
        assertThat(blank.hasValidAnthropicKey()).isFalse();
        assertThat(blank.hasValidGroqKey()).isFalse();
        assertThat(fake.hasValidOpenAiKey()).isFalse();
        assertThat(fake.hasValidAnthropicKey()).isFalse();
        assertThat(fake.hasValidGroqKey()).isFalse();
    }
}
