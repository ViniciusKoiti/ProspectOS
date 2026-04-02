package dev.prospectos.infrastructure.mcp.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import dev.prospectos.infrastructure.mcp.security.McpApiKeyAuthenticationFilter;
import dev.prospectos.infrastructure.mcp.security.McpRateLimitingFilter;

class McpSecurityConfigTest {

    private final McpSecurityConfig config = new McpSecurityConfig();

    @Test
    void shouldRegisterRateLimitingFilterForMcpEndpoint() {
        var registration = config.mcpRateLimitingFilterRegistration(mock(McpRateLimitingFilter.class));

        assertThat(registration.getFilter()).isNotNull();
        assertThat(registration.getFilterName()).isEqualTo("mcpRateLimitingFilter");
        assertThat(registration.getOrder()).isEqualTo(1);
        assertThat(registration.getUrlPatterns()).containsExactly("/mcp/*");
    }

    @Test
    void shouldRegisterApiKeyFilterForMcpEndpoint() {
        var registration = config.mcpApiKeyAuthenticationFilterRegistration(mock(McpApiKeyAuthenticationFilter.class));

        assertThat(registration.getFilter()).isNotNull();
        assertThat(registration.getFilterName()).isEqualTo("mcpApiKeyAuthenticationFilter");
        assertThat(registration.getOrder()).isEqualTo(2);
        assertThat(registration.getUrlPatterns()).containsExactly("/mcp/*");
    }
}
