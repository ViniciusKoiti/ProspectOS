package dev.prospectos.ai.config;

import dev.prospectos.ai.client.LLMProvider;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 * Condition backed by prospectos.ai.active-providers.
 */
public class OnActiveAIProviderCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnActiveAIProvider.class.getName());
        if (attributes == null) {
            return ConditionOutcome.noMatch(ConditionMessage.empty());
        }

        LLMProvider provider = (LLMProvider) attributes.get("value");
        String configuredProviders = context.getEnvironment().getProperty(
            AIConfigurationProperties.ACTIVE_PROVIDERS,
            AIConfigurationProperties.DEFAULT_ACTIVE_PROVIDERS
        );

        boolean active = AIProviderActivationProperties.parse(configuredProviders).contains(provider);
        ConditionMessage.Builder message = ConditionMessage.forCondition(
            ConditionalOnActiveAIProvider.class,
            provider.name().toLowerCase()
        );

        if (active) {
            return ConditionOutcome.match(message.found("active provider").items(provider.name().toLowerCase()));
        }
        return ConditionOutcome.noMatch(
            message.because(AIConfigurationProperties.ACTIVE_PROVIDERS + "='" + configuredProviders + "'")
        );
    }
}
