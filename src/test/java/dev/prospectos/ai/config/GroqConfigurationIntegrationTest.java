package dev.prospectos.ai.config;

import dev.prospectos.ai.client.LLMProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class GroqConfigurationIntegrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(GroqConditionTestConfiguration.class);

    @Test
    void registersGroqConditionalBeanWhenGroqIsActive() {
        contextRunner
            .withPropertyValues("prospectos.ai.active-providers=groq,openai")
            .run(context -> {
                assertThat(context).hasNotFailed();
                assertThat(context).hasBean("groqConditionalBean");
                assertThat(context).doesNotHaveBean("anthropicConditionalBean");
            });
    }

    @Test
    void doesNotRegisterGroqConditionalBeanWhenGroqIsNotActive() {
        contextRunner
            .withPropertyValues("prospectos.ai.active-providers=openai,anthropic")
            .run(context -> {
                assertThat(context).hasNotFailed();
                assertThat(context).doesNotHaveBean("groqConditionalBean");
                assertThat(context).hasBean("anthropicConditionalBean");
            });
    }

    @Test
    void acceptsHyphenSeparatedProviderValues() {
        contextRunner
            .withPropertyValues("prospectos.ai.active-providers=groq,mock")
            .run(context -> {
                assertThat(context).hasNotFailed();
                assertThat(context).hasBean("groqConditionalBean");
            });
    }

    @Configuration
    @EnableConfigurationProperties(AIProviderActivationProperties.class)
    static class GroqConditionTestConfiguration {

        @Bean("groqConditionalBean")
        @ConditionalOnActiveAIProvider(LLMProvider.GROQ)
        String groqConditionalBean() {
            return "groq";
        }

        @Bean("anthropicConditionalBean")
        @ConditionalOnActiveAIProvider(LLMProvider.ANTHROPIC)
        String anthropicConditionalBean() {
            return "anthropic";
        }
    }
}
