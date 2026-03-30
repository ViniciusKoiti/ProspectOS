package dev.prospectos.infrastructure.mcp.security;

import dev.prospectos.infrastructure.mcp.config.McpSecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Profile("mcp")
@RequiredArgsConstructor
public class McpApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String MCP_CLIENT_ID_ATTRIBUTE = "prospectos.mcp.clientId";

    private final McpSecurityProperties mcpSecurityProperties;
    private final McpAuditService mcpAuditService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (isHealthCheck(request) || !isMcpEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        var apiKey = extractApiKey(request);
        if (!isValidApiKey(apiKey)) {
            log.warn("MCP authentication failed: Invalid or missing API key for request {}", request.getRequestURI());
            mcpAuditService.logAuthentication(request, apiKey, false);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Authentication failed\",\"message\":\"Invalid or missing API key\"}");
            return;
        }
        request.setAttribute(MCP_CLIENT_ID_ATTRIBUTE, "api-key:" + apiKey.substring(0, Math.min(8, apiKey.length())) + "...");
        mcpAuditService.logAuthentication(request, apiKey, true);
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !isMcpEndpoint(request) && !isHealthCheck(request);
    }

    private boolean isHealthCheck(HttpServletRequest request) {
        return request.getRequestURI().contains("/actuator/health");
    }

    private boolean isMcpEndpoint(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/mcp/");
    }

    private String extractApiKey(HttpServletRequest request) {
        var apiKey = request.getHeader(mcpSecurityProperties.apiKeyHeader());
        if (!StringUtils.hasText(apiKey)) {
            var authorization = request.getHeader("Authorization");
            if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
                apiKey = authorization.substring(7);
            }
        }
        return StringUtils.hasText(apiKey) ? apiKey.trim() : null;
    }

    private boolean isValidApiKey(String apiKey) {
        if (!StringUtils.hasText(apiKey)) {
            return false;
        }
        if (!mcpSecurityProperties.allowedApiKeys().isEmpty()) {
            return mcpSecurityProperties.allowedApiKeys().contains(apiKey);
        }
        log.warn("No MCP API keys configured; accepting any non-empty key for local usage");
        return true;
    }
}
