package dev.prospectos.ai.config;

import dev.prospectos.ai.client.LLMProvider;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Activates a bean only when the given provider is present in
 * prospectos.ai.active-providers.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnActiveAIProviderCondition.class)
public @interface ConditionalOnActiveAIProvider {

    LLMProvider value();
}
