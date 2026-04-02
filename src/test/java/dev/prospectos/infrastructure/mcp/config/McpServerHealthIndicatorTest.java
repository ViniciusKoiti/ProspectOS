package dev.prospectos.infrastructure.mcp.config;

import dev.prospectos.api.mcp.QueryMetricsService;
import dev.prospectos.api.mcp.QueryMetricsSnapshot;
import dev.prospectos.api.mcp.QueryTimeWindow;
import dev.prospectos.infrastructure.mcp.tools.QueryMetricsMcpTools;
import dev.prospectos.infrastructure.service.compliance.AllowedSourcesProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class McpServerHealthIndicatorTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(McpHealthTestConfiguration.class)
        .withPropertyValues(
            "spring.ai.mcp.server.enabled=true",
            "spring.ai.mcp.server.name=test-mcp-server",
            "spring.ai.mcp.server.protocol=STREAMABLE",
            "spring.ai.mcp.server.stdio=true",
            "spring.ai.mcp.server.streamable-http.mcp-endpoint=/mcp"
        );

    @Test
    void reportsMcpHealthWithRegisteredTools() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(McpServerHealthIndicator.class);

            var health = context.getBean(McpServerHealthIndicator.class).health();

            assertThat(health.getStatus()).isEqualTo(Status.UP);
            assertThat(health.getDetails()).containsEntry("serverName", "test-mcp-server");
            assertThat(health.getDetails()).containsEntry("protocol", "STREAMABLE");
            assertThat(health.getDetails()).containsEntry("endpoint", "/mcp");
            assertThat(health.getDetails()).containsEntry("stdioEnabled", true);
            assertThat(health.getDetails()).containsEntry("toolCount", 1);
        });
    }

    @Configuration
    static class McpHealthTestConfiguration {

        @Bean
        McpServerHealthIndicator mcpServerHealthIndicator(
            org.springframework.context.ApplicationContext applicationContext,
            org.springframework.core.env.Environment environment
        ) {
            return new McpServerHealthIndicator(applicationContext, environment);
        }

        @Bean
        QueryMetricsMcpTools queryMetricsMcpTools(QueryMetricsService queryMetricsService) {
            return new QueryMetricsMcpTools(
                queryMetricsService,
                new AllowedSourcesProperties(List.of("amazon-location", "vector-company"), List.of("amazon-location"))
            );
        }

        @Bean
        QueryMetricsService queryMetricsService() {
            return new QueryMetricsService() {
                @Override
                public QueryMetricsSnapshot getMetrics(QueryTimeWindow timeWindow, String provider) {
                    return new QueryMetricsSnapshot(
                        10,
                        new BigDecimal("1.50"),
                        new BigDecimal("0.1500"),
                        0.9,
                        900,
                        new QueryMetricsSnapshot.Trends("+1%", "+1%", "+1%"),
                        List.of(new QueryMetricsSnapshot.ProviderMetric("amazon-location", 10, new BigDecimal("1.50"), 0.9, 900))
                    );
                }
            };
        }
    }
}
