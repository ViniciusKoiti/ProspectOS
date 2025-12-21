package dev.prospectos.config;


import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final Map<String, String> ENV_TO_PROPERTY = buildEnvMappings();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        Map<String, Object> props = new HashMap<>();
        for (Map.Entry<String, String> mapping : ENV_TO_PROPERTY.entrySet()) {
            String value = dotenv.get(mapping.getKey(), null);
            if (value != null && !value.isBlank()) {
                props.putIfAbsent(mapping.getValue(), value);
            }
        }

        if (!props.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource("dotenv", props));
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private static Map<String, String> buildEnvMappings() {
        Map<String, String> mappings = new LinkedHashMap<>();

        // AI providers
        mappings.put("SPRING_AI_OPENAI_API_KEY", "spring.ai.openai.api-key");
        mappings.put("SPRING_AI_OPENAI_CHAT_MODEL", "spring.ai.openai.chat.model");
        mappings.put("SPRING_AI_OPENAI_CHAT_OPTIONS_TEMPERATURE", "spring.ai.openai.chat.options.temperature");
        mappings.put("SPRING_AI_OPENAI_CHAT_OPTIONS_MAX_TOKENS", "spring.ai.openai.chat.options.max-tokens");
        mappings.put("SPRING_AI_ANTHROPIC_API_KEY", "spring.ai.anthropic.api-key");
        mappings.put("SPRING_AI_ANTHROPIC_CLAUDE_MODEL", "spring.ai.anthropic.claude.model");

        // Backwards-compatible keys
        mappings.put("OPENAI_API_KEY", "spring.ai.openai.api-key");
        mappings.put("ANTHROPIC_API_KEY", "spring.ai.anthropic.api-key");

        // Application configuration
        mappings.put("SPRING_PROFILES_ACTIVE", "spring.profiles.active");
        mappings.put("SERVER_PORT", "server.port");
        mappings.put("LOGGING_LEVEL_ROOT", "logging.level.root");
        mappings.put("LOGGING_LEVEL_DEV_PROSPECTOS", "logging.level.dev.prospectos");
        mappings.put("LOGGING_LEVEL_DEV_PROSPECTOS_AI", "logging.level.dev.prospectos.ai");

        // Database configuration
        mappings.put("SPRING_DATASOURCE_URL", "spring.datasource.url");
        mappings.put("SPRING_DATASOURCE_DRIVER_CLASS_NAME", "spring.datasource.driver-class-name");
        mappings.put("SPRING_DATASOURCE_USERNAME", "spring.datasource.username");
        mappings.put("SPRING_DATASOURCE_PASSWORD", "spring.datasource.password");
        mappings.put("SPRING_H2_CONSOLE_ENABLED", "spring.h2.console.enabled");
        mappings.put("SPRING_JPA_DATABASE_PLATFORM", "spring.jpa.database-platform");
        mappings.put("SPRING_JPA_HIBERNATE_DDL_AUTO", "spring.jpa.hibernate.ddl-auto");
        mappings.put("SPRING_JPA_SHOW_SQL", "spring.jpa.show-sql");

        // External services
        mappings.put("SCRAPER_SERVICE_URL", "scraper.service.url");
        mappings.put("SCRAPER_SERVICE_TIMEOUT", "scraper.service.timeout");
        mappings.put("NEWS_API_KEY", "news.api.key");
        mappings.put("NEWS_API_URL", "news.api.url");

        // Security
        mappings.put("JWT_SECRET", "jwt.secret");
        mappings.put("JWT_EXPIRATION", "jwt.expiration");
        mappings.put("CORS_ALLOWED_ORIGINS", "cors.allowed.origins");
        mappings.put("CORS_ALLOWED_METHODS", "cors.allowed.methods");
        mappings.put("CORS_ALLOWED_HEADERS", "cors.allowed.headers");

        // Monitoring and observability
        mappings.put("MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE", "management.endpoints.web.exposure.include");
        mappings.put("MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS", "management.endpoint.health.show-details");
        mappings.put("MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED", "management.metrics.export.prometheus.enabled");

        // AI behavior
        mappings.put("MOCK_AI_RESPONSES_ENABLED", "mock.ai.responses.enabled");
        mappings.put("MOCK_AI_RESPONSE_DELAY", "mock.ai.response.delay");
        mappings.put("AI_REQUEST_TIMEOUT", "ai.request.timeout");
        mappings.put("AI_MAX_RETRIES", "ai.max.retries");
        mappings.put("AI_PROVIDER_PRIORITY", "ai.provider.priority");

        // Feature flags
        mappings.put("FEATURE_AI_ENRICHMENT_ENABLED", "feature.ai.enrichment.enabled");
        mappings.put("FEATURE_AUTO_SCORING_ENABLED", "feature.auto.scoring.enabled");
        mappings.put("FEATURE_OUTREACH_GENERATION_ENABLED", "feature.outreach.generation.enabled");
        mappings.put("FEATURE_SIGNAL_DETECTION_ENABLED", "feature.signal.detection.enabled");

        // Development tools
        mappings.put("SPRING_DEVTOOLS_RESTART_ENABLED", "spring.devtools.restart.enabled");
        mappings.put("SPRING_DEVTOOLS_LIVERELOAD_ENABLED", "spring.devtools.livereload.enabled");

        return mappings;
    }
}
