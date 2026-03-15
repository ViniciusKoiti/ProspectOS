package dev.prospectos.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    private static final String[] DEFAULT_METHODS = {"GET", "POST", "PUT", "DELETE", "OPTIONS"};
    private static final String[] DEFAULT_HEADERS = {"*"};

    private final String allowedOrigins;
    private final String allowedMethods;
    private final String allowedHeaders;

    public WebCorsConfig(
        @Value("${cors.allowed.origins:http://localhost:3000,http://localhost:5173}") String allowedOrigins,
        @Value("${cors.allowed.methods:GET,POST,PUT,DELETE,OPTIONS}") String allowedMethods,
        @Value("${cors.allowed.headers:*}") String allowedHeaders
    ) {
        this.allowedOrigins = allowedOrigins;
        this.allowedMethods = allowedMethods;
        this.allowedHeaders = allowedHeaders;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins(parseCsv(allowedOrigins))
            .allowedMethods(parseCsvOrDefault(allowedMethods, DEFAULT_METHODS))
            .allowedHeaders(parseCsvOrDefault(allowedHeaders, DEFAULT_HEADERS));
    }

    private String[] parseCsvOrDefault(String raw, String[] fallback) {
        String[] values = parseCsv(raw);
        return values.length == 0 ? fallback : values;
    }

    private String[] parseCsv(String raw) {
        if (raw == null || raw.isBlank()) {
            return new String[0];
        }
        return Arrays.stream(raw.split(","))
            .map(String::trim)
            .filter(token -> !token.isEmpty())
            .toArray(String[]::new);
    }
}
