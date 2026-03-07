package dev.prospectos.ai.config;

import dev.prospectos.ai.service.AIPromptService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AIProviderSelectionIntegrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(
            AIProviderSelectionTestConfiguration.class,
            ChatClientConfig.class,
            AIChatClientConfig.class,
            AIPromptService.class
        );

    @Test
    void selectsGroqWhenGroqIsFirstActiveProviderAndKeyIsValid() {
        contextRunner
            .withPropertyValues(
                "prospectos.ai.enabled=true",
                "prospectos.ai.active-providers=groq,openai,anthropic",
                "prospectos.ai.groq.api-key=gsk-live-key",
                "spring.ai.openai.api-key=sk-live-key",
                "spring.ai.anthropic.api-key=sk-ant-live-key"
            )
            .run(context -> {
                assertThat(context).hasNotFailed();
                assertThat(context).hasBean("chatClient");
                assertThat(context).hasBean("scoringChatClient");
                assertThat(context).hasBean("groqChatClient");
                assertThat(context).hasBean("groqScoringChatClient");

                ChatModel primaryChatModel = context.getBean("primaryChatModel", ChatModel.class);
                ChatModel groqChatModel = context.getBean("groqChatModel", ChatModel.class);

                assertThat(primaryChatModel).isSameAs(groqChatModel);
            });
    }

    @Test
    void fallsBackToOpenAiWhenGroqKeyIsInvalid() {
        contextRunner
            .withPropertyValues(
                "prospectos.ai.enabled=true",
                "prospectos.ai.active-providers=groq,openai",
                "prospectos.ai.groq.api-key=mock-key",
                "spring.ai.openai.api-key=sk-live-key"
            )
            .run(context -> {
                assertThat(context).hasNotFailed();

                ChatModel primaryChatModel = context.getBean("primaryChatModel", ChatModel.class);
                ChatModel openAiChatModel = context.getBean("openAiChatModel", ChatModel.class);

                assertThat(primaryChatModel).isSameAs(openAiChatModel);
            });
    }

    @Test
    void doesNotCreateAiBeansWhenApplicationAiIsDisabled() {
        contextRunner
            .withPropertyValues(
                "prospectos.ai.enabled=false",
                "prospectos.ai.active-providers=openai",
                "spring.ai.openai.api-key=sk-live-key"
            )
            .run(context -> {
                assertThat(context).hasNotFailed();
                assertThat(context).doesNotHaveBean(ChatClient.class);
                assertThat(context).hasBean("primaryChatModel");
            });
    }

    @Test
    void failsStartupWhenNoActiveProviderIsAvailable() {
        contextRunner
            .withPropertyValues(
                "prospectos.ai.enabled=true",
                "prospectos.ai.active-providers=openai",
                "spring.ai.openai.api-key=mock-key"
            )
            .run(context -> {
                assertThat(context).hasFailed();
                Throwable rootCause = rootCause(context.getStartupFailure());
                assertThat(rootCause).isInstanceOf(IllegalStateException.class);
                assertThat(rootCause.getMessage()).contains("No active LLM provider is available");
            });
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current != null && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    @Configuration
    @EnableConfigurationProperties(AIProviderActivationProperties.class)
    static class AIProviderSelectionTestConfiguration {

        @Bean("openAiChatModel")
        ChatModel openAiChatModel() {
            return mock(ChatModel.class, "openAiChatModel");
        }

        @Bean("groqChatModel")
        ChatModel groqChatModel() {
            return mock(ChatModel.class, "groqChatModel");
        }

        @Bean("anthropicChatModel")
        ChatModel anthropicChatModel() {
            return mock(ChatModel.class, "anthropicChatModel");
        }
    }
}
