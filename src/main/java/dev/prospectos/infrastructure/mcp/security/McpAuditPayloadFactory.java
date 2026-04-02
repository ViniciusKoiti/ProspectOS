package dev.prospectos.infrastructure.mcp.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
class McpAuditPayloadFactory {

    Map<String, Object> basePayload(HttpServletRequest request) {
        var payload = new HashMap<String, Object>();
        payload.put("timestamp", Instant.now());
        payload.put("remoteAddr", clientIpAddress(request));
        payload.put("userAgent", request.getHeader("User-Agent"));
        payload.put("method", request.getMethod());
        payload.put("uri", request.getRequestURI());
        payload.put("queryString", request.getQueryString());
        return payload;
    }

    Map<String, Object> sanitizeParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            return Map.of();
        }
        var sanitized = new HashMap<>(parameters);
        sanitized.entrySet().removeIf(entry -> sensitive(entry.getKey()));
        sanitized.replaceAll((key, value) -> key.toLowerCase().contains("key") && value instanceof String stringValue ? maskApiKey(stringValue) : value);
        return sanitized;
    }

    String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return "***";
        }
        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }

    private boolean sensitive(String key) {
        var normalized = key.toLowerCase();
        return normalized.contains("password") || normalized.contains("secret") || normalized.contains("token");
    }

    private String clientIpAddress(HttpServletRequest request) {
        var xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        var xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
