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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class McpRateLimitingFilterTest {

    @Mock
    private McpAuditService mcpAuditService;

    private McpRateLimitingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new McpRateLimitingFilter(properties(), mcpAuditService, new McpRateLimiter());
    }

    @Test
    void allowsRequestWithinLimitAndAddsHeaders() throws Exception {
        var request = new MockHttpServletRequest("GET", "/mcp/tools/list");
        request.addHeader("X-MCP-API-KEY", "client-12345");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("X-RateLimit-Limit")).isEqualTo("1");
        assertThat(response.getHeader("X-RateLimit-Remaining")).isEqualTo("0");
        assertThat(chain.getRequest()).isSameAs(request);
    }

    @Test
    void rejectsRequestsAfterBurstCapacityIsExhausted() throws Exception {
        var firstRequest = new MockHttpServletRequest("GET", "/mcp/tools/list");
        firstRequest.addHeader("X-MCP-API-KEY", "client-12345");
        filter.doFilter(firstRequest, new MockHttpServletResponse(), new MockFilterChain());

        var secondRequest = new MockHttpServletRequest("GET", "/mcp/tools/list");
        secondRequest.addHeader("X-MCP-API-KEY", "client-12345");
        var secondResponse = new MockHttpServletResponse();
        var secondChain = new MockFilterChain();

        filter.doFilter(secondRequest, secondResponse, secondChain);

        assertThat(secondResponse.getStatus()).isEqualTo(429);
        assertThat(secondResponse.getHeader("Retry-After")).isNotBlank();
        assertThat(secondResponse.getContentAsString()).contains("Rate limit exceeded");
        assertThat(secondChain.getRequest()).isNull();
        verify(mcpAuditService).logRateLimitViolation(eq(secondRequest), any(String.class));
    }

    private McpSecurityProperties properties() {
        return new McpSecurityProperties(
            true,
            "X-MCP-API-KEY",
            Set.of("client-12345"),
            new McpSecurityProperties.RateLimit(1, 1, Duration.ofMinutes(1), Duration.ofHours(1)),
            new McpSecurityProperties.Audit(true, true, true)
        );
    }
}

