package dev.prospectos.infrastructure.mcp.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ConditionalOnProperty(prefix = "spring.ai.mcp.server", name = "enabled", havingValue = "true")
public @interface ConditionalOnMcpEnabled {
}
