package dev.prospectos.infrastructure.service.outreach;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class ProviderConstructorInjectionContextTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
        .withUserConfiguration(TestConfig.class)
        .withPropertyValues("prospectos.outreach.resend.enabled=false");

    @Test
    void createsResendProviderWithoutDefaultConstructor() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ResendOutreachDeliveryService.class);
        });
    }

    @Configuration
    static class TestConfig {

        @Bean
        RestTemplateBuilder restTemplateBuilder() {
            return new RestTemplateBuilder();
        }

        @Bean
        ResendProperties resendProperties() {
            return new ResendProperties(false, "", null, null);
        }

        @Bean
        ResendOutreachDeliveryService resendOutreachDeliveryService(RestTemplateBuilder builder, ResendProperties properties) {
            return new ResendOutreachDeliveryService(builder, properties);
        }
    }
}
