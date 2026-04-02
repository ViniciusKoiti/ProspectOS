package dev.prospectos.infrastructure.mcp.security;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import dev.prospectos.infrastructure.mcp.config.McpSecurityProperties;

class McpAuditServiceTest {

    @Test
    void shouldLogAuditEventsWhenAuditIsEnabled() {
        var audit = new McpAuditService(properties(true), new McpAuditPayloadFactory());
        var request = request();

        assertThatCode(() -> audit.logAuthentication(request, "abcd1234wxyz9876", true)).doesNotThrowAnyException();
        assertThatCode(() -> audit.logAuthentication(request, "abcd1234wxyz9876", false)).doesNotThrowAnyException();
        assertThatCode(() -> audit.logRateLimitViolation(request, "client-1")).doesNotThrowAnyException();
        assertThatCode(() -> audit.logToolExecution(
            "get_query_metrics",
            Map.of("apiKey", "abcd1234wxyz9876", "password", "secret", "region", "br"),
            true,
            25L,
            "client-1"
        )).doesNotThrowAnyException();
        assertThatCode(() -> audit.logResourceAccess("query-history://24h/all", false, 12L, "client-1")).doesNotThrowAnyException();
        assertThatCode(() -> audit.logSecurityEvent("RATE_LIMIT", "Too many requests", request)).doesNotThrowAnyException();
    }

    @Test
    void shouldSkipLoggingWhenAuditIsDisabled() {
        var audit = new McpAuditService(properties(false), new McpAuditPayloadFactory());

        assertThatCode(() -> audit.logAuthentication(request(), "short", true)).doesNotThrowAnyException();
        assertThatCode(() -> audit.logToolExecution("tool", null, false, 1L, "client-2")).doesNotThrowAnyException();
    }

    private McpSecurityProperties properties(boolean auditEnabled) {
        return new McpSecurityProperties(
            true,
            "X-MCP-API-KEY",
            Set.of("key"),
            new McpSecurityProperties.RateLimit(100, 20, Duration.ofMinutes(1), Duration.ofHours(1)),
            new McpSecurityProperties.Audit(auditEnabled, true, true)
        );
    }

    private MockHttpServletRequest request() {
        var request = new MockHttpServletRequest("POST", "/mcp/tools");
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("User-Agent", "JUnit");
        request.addHeader("X-Forwarded-For", "203.0.113.10, 10.0.0.1");
        request.addHeader("X-Real-IP", "198.51.100.5");
        request.setQueryString("tool=get_query_metrics");
        return request;
    }
}
