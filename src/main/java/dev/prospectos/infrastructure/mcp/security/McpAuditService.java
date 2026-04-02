package dev.prospectos.infrastructure.mcp.security;

import dev.prospectos.infrastructure.mcp.config.ConditionalOnMcpEnabled;
import dev.prospectos.infrastructure.mcp.config.McpSecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@ConditionalOnMcpEnabled
@RequiredArgsConstructor
public class McpAuditService {

    private final McpSecurityProperties mcpSecurityProperties;
    private final McpAuditPayloadFactory payloadFactory;

    public void logAuthentication(HttpServletRequest request, String apiKey, boolean success) {
        if (shouldSkip(success)) {
            return;
        }
        var payload = payloadFactory.basePayload(request);
        payload.put("eventType", "AUTHENTICATION");
        payload.put("success", success);
        payload.put("apiKeyPrefix", payloadFactory.maskApiKey(apiKey));
        logByOutcome(success, "MCP Authentication successful: {}", "MCP Authentication failed: {}", payload);
    }

    public void logRateLimitViolation(HttpServletRequest request, String clientId) {
        if (!mcpSecurityProperties.audit().enabled()) {
            return;
        }
        var payload = payloadFactory.basePayload(request);
        payload.put("eventType", "RATE_LIMIT_VIOLATION");
        payload.put("clientId", clientId);
        log.warn("MCP Rate limit violation: {}", payload);
    }

    public void logToolExecution(String toolName, Map<String, Object> parameters, boolean success, long durationMs, String clientId) {
        logStructuredEvent("TOOL_EXECUTION", toolName, parameters, success, durationMs, clientId, "toolName");
    }

    public void logResourceAccess(String resourceUri, boolean success, long durationMs, String clientId) {
        logStructuredEvent("RESOURCE_ACCESS", resourceUri, null, success, durationMs, clientId, "resourceUri");
    }

    public void logSecurityEvent(String eventType, String description, HttpServletRequest request) {
        if (!mcpSecurityProperties.audit().enabled()) {
            return;
        }
        var payload = payloadFactory.basePayload(request);
        payload.put("eventType", "SECURITY_EVENT");
        payload.put("securityEventType", eventType);
        payload.put("description", description);
        log.warn("MCP Security event: {}", payload);
    }

    private void logStructuredEvent(String eventType, String resource, Map<String, Object> parameters, boolean success, long durationMs, String clientId, String key) {
        if (shouldSkip(success)) {
            return;
        }
        var payload = new HashMap<String, Object>();
        payload.put("timestamp", Instant.now());
        payload.put("eventType", eventType);
        payload.put(key, resource);
        payload.put("parameters", payloadFactory.sanitizeParameters(parameters));
        payload.put("success", success);
        payload.put("durationMs", durationMs);
        payload.put("clientId", clientId);
        logByOutcome(success, "MCP event: {}", "MCP event failed: {}", payload);
    }

    private boolean shouldSkip(boolean success) {
        if (!mcpSecurityProperties.audit().enabled()) {
            return true;
        }
        return success ? !mcpSecurityProperties.audit().logSuccessfulRequests() : !mcpSecurityProperties.audit().logFailedRequests();
    }

    private void logByOutcome(boolean success, String successMessage, String failureMessage, Map<String, Object> payload) {
        if (success) {
            log.info(successMessage, payload);
            return;
        }
        log.warn(failureMessage, payload);
    }
}




