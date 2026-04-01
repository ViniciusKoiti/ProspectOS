package dev.prospectos.infrastructure.mcp.security;

import dev.prospectos.infrastructure.mcp.config.ConditionalOnMcpEnabled;
import dev.prospectos.infrastructure.mcp.config.McpSecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@ConditionalOnMcpEnabled
@RequiredArgsConstructor
public class McpRateLimitingFilter extends OncePerRequestFilter {

    private final McpSecurityProperties mcpSecurityProperties;
    private final McpAuditService mcpAuditService;
    private final McpRateLimiter rateLimiter;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (isHealthCheck(request) || !isMcpEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        var clientId = rateLimiter.clientId(request, mcpSecurityProperties.apiKeyHeader());
        var result = rateLimiter.check(clientId, mcpSecurityProperties.rateLimit());
        if (!result.allowed()) {
            log.warn("Rate limit exceeded for client {} on endpoint {}", clientId, request.getRequestURI());
            mcpAuditService.logRateLimitViolation(request, clientId);
            rateLimiter.writeExceededResponse(response, mcpSecurityProperties.rateLimit(), result);
            return;
        }
        rateLimiter.applyHeaders(response, mcpSecurityProperties.rateLimit(), result);
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !isMcpEndpoint(request);
    }

    private boolean isHealthCheck(HttpServletRequest request) {
        return request.getRequestURI().contains("/actuator/health");
    }

    private boolean isMcpEndpoint(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/mcp/");
    }
}




