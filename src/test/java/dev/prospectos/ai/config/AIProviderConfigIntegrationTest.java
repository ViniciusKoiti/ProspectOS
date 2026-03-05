package dev.prospectos.ai.config;

import dev.prospectos.ai.client.AIProvider;
import dev.prospectos.ai.factory.AIProviderFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class AIProviderConfigIntegrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(AIProviderConfig.class, AIProviderConfigTestConfiguration.class);

    @Test
    void createsAiProviderBeanWhenAiIsEnabled() {
        contextRunner
            .withPropertyValues("prospectos.ai.enabled=true")
            .run(context -> {
                assertThat(context).hasNotFailed();
                assertThat(context).hasSingleBean(AIProvider.class);
            });
    }

    @Test
    void doesNotCreateAiProviderBeanWhenAiIsDisabled() {
        contextRunner
            .withPropertyValues("prospectos.ai.enabled=false")
            .run(context -> {
                assertThat(context).hasNotFailed();
                assertThat(context).doesNotHaveBean(AIProvider.class);
            });
    }

    @Configuration
    static class AIProviderConfigTestConfiguration {

        @Bean
        AIProviderFactory aiProviderFactory() {
            AIProviderFactory factory = mock(AIProviderFactory.class);
            AIProvider provider = mock(AIProvider.class);
            given(factory.createPrimaryProvider()).willReturn(provider);
            return factory;
        }
    }
}
