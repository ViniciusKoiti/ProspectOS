package dev.prospectos.infrastructure.mcp.config;

import dev.prospectos.infrastructure.mcp.security.McpApiKeyAuthenticationFilter;
import dev.prospectos.infrastructure.mcp.security.McpRateLimitingFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers MCP servlet filters for rate limiting and API-key authentication.
 */
@Configuration
@ConditionalOnMcpEnabled
@ConditionalOnProperty(name = "spring.ai.mcp.server.security.enabled", havingValue = "true", matchIfMissing = true)
public class McpSecurityConfig {

    @Bean
    FilterRegistrationBean<McpRateLimitingFilter> mcpRateLimitingFilterRegistration(McpRateLimitingFilter filter) {
        var registration = new FilterRegistrationBean<>(filter);
        registration.setName("mcpRateLimitingFilter");
        registration.setOrder(1);
        registration.addUrlPatterns("/mcp/*");
        return registration;
    }

    @Bean
    FilterRegistrationBean<McpApiKeyAuthenticationFilter> mcpApiKeyAuthenticationFilterRegistration(
        McpApiKeyAuthenticationFilter filter
    ) {
        var registration = new FilterRegistrationBean<>(filter);
        registration.setName("mcpApiKeyAuthenticationFilter");
        registration.setOrder(2);
        registration.addUrlPatterns("/mcp/*");
        return registration;
    }
}



