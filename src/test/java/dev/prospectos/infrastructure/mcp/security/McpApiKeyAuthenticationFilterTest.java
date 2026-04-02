package dev.prospectos.infrastructure.mcp.security;

import dev.prospectos.infrastructure.mcp.config.McpSecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class McpApiKeyAuthenticationFilterTest {

    @Mock
    private McpAuditService mcpAuditService;

    private McpApiKeyAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new McpApiKeyAuthenticationFilter(properties(), mcpAuditService);
    }

    @Test
    void authenticatesValidApiKeyAndSetsClientAttribute() throws Exception {
        var request = new MockHttpServletRequest("POST", "/mcp/tools/call");
        request.addHeader("X-MCP-API-KEY", "valid-key-123");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(request.getAttribute(McpApiKeyAuthenticationFilter.MCP_CLIENT_ID_ATTRIBUTE))
            .isEqualTo("api-key:valid-ke...");
        verify(mcpAuditService).logAuthentication(eq(request), eq("valid-key-123"), eq(true));
    }

    @Test
    void rejectsMissingApiKey() throws Exception {
        var request = new MockHttpServletRequest("POST", "/mcp/tools/call");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Authentication failed");
        assertThat(chain.getRequest()).isNull();
        verify(mcpAuditService).logAuthentication(eq(request), eq(null), eq(false));
    }

    @Test
    void acceptsBearerTokenFallback() throws Exception {
        var request = new MockHttpServletRequest("POST", "/mcp/tools/call");
        request.addHeader("Authorization", "Bearer valid-key-123");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(mcpAuditService).logAuthentication(eq(request), eq("valid-key-123"), eq(true));
    }

    @Test
    void skipsHealthEndpoint() throws Exception {
        var request = new MockHttpServletRequest("GET", "/actuator/health/mcp");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isSameAs(request);
        verifyNoInteractions(mcpAuditService);
    }

    private McpSecurityProperties properties() {
        return new McpSecurityProperties(
            true,
            "X-MCP-API-KEY",
            Set.of("valid-key-123"),
            new McpSecurityProperties.RateLimit(100, 20, Duration.ofMinutes(1), Duration.ofHours(1)),
            new McpSecurityProperties.Audit(true, true, true)
        );
    }
}
