package dev.prospectos.infrastructure.mcp.config;

import org.springaicommunity.mcp.annotation.McpResource;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.MethodIntrospector;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.util.Objects;

@Component("mcpServer")
@Profile("mcp")
@RequiredArgsConstructor
public class McpServerHealthIndicator extends AbstractHealthIndicator {

    private final ApplicationContext applicationContext;
    private final Environment environment;

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        int toolCount = annotatedMethodCount(McpTool.class);
        int resourceCount = annotatedMethodCount(McpResource.class);

        builder.up()
            .withDetail("serverName", environment.getProperty("spring.ai.mcp.server.name", "prospectos-mcp-server"))
            .withDetail("protocol", environment.getProperty("spring.ai.mcp.server.protocol", "STREAMABLE"))
            .withDetail("endpoint", environment.getProperty("spring.ai.mcp.server.streamable-http.mcp-endpoint", "/mcp"))
            .withDetail("stdioEnabled", environment.getProperty("spring.ai.mcp.server.stdio", Boolean.class, false))
            .withDetail("toolCount", toolCount)
            .withDetail("resourceCount", resourceCount);
    }

    private int annotatedMethodCount(Class<? extends Annotation> annotationType) {
        return (int) java.util.Arrays.stream(applicationContext.getBeanDefinitionNames())
            .map(applicationContext::getType)
            .filter(Objects::nonNull)
            .mapToLong(type -> MethodIntrospector.selectMethods(
                type,
                (MethodIntrospector.MetadataLookup<Boolean>) method ->
                    AnnotatedElementUtils.hasAnnotation(method, annotationType) ? Boolean.TRUE : null
            ).size())
            .sum();
    }
}
