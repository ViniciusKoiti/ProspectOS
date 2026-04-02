package dev.prospectos.config;

import java.util.LinkedHashMap;
import java.util.Map;

final class DotenvEnvironmentMappings {

    private DotenvEnvironmentMappings() {
    }

    static Map<String, String> ordered() {
        Map<String, String> mappings = new LinkedHashMap<>();
        mappings.put("SPRING_AI_OPENAI_API_KEY", "spring.ai.openai.api-key");
        mappings.put("SPRING_AI_OPENAI_CHAT_MODEL", "spring.ai.openai.chat.model");
        mappings.put("SPRING_AI_OPENAI_CHAT_OPTIONS_TEMPERATURE", "spring.ai.openai.chat.options.temperature");
        mappings.put("SPRING_AI_OPENAI_CHAT_OPTIONS_MAX_TOKENS", "spring.ai.openai.chat.options.max-tokens");
        mappings.put("SPRING_AI_OPENAI_ENABLED", "spring.ai.openai.enabled");
        mappings.put("SPRING_AI_ANTHROPIC_API_KEY", "spring.ai.anthropic.api-key");
        mappings.put("SPRING_AI_ANTHROPIC_CLAUDE_MODEL", "spring.ai.anthropic.claude.model");
        mappings.put("SPRING_AI_ANTHROPIC_ENABLED", "spring.ai.anthropic.enabled");
        mappings.put("PROSPECTOS_AI_ENABLED", "prospectos.ai.enabled");
        mappings.put("PROSPECTOS_AI_ACTIVE_PROVIDERS", "prospectos.ai.active-providers");
        mappings.put("PROSPECTOS_AI_GROQ_API_KEY", "prospectos.ai.groq.api-key");
        mappings.put("PROSPECTOS_AI_GROQ_BASE_URL", "prospectos.ai.groq.base-url");
        mappings.put("PROSPECTOS_AI_GROQ_MODEL", "prospectos.ai.groq.model");
        mappings.put("SCRAPER_AI_ENABLED", "scraper.ai.enabled");
        mappings.put("PROSPECTOS_LEADS_GOOGLE_PLACES_ENABLED", "prospectos.leads.google-places.enabled");
        mappings.put("PROSPECTOS_LEADS_GOOGLE_PLACES_API_KEY", "prospectos.leads.google-places.api-key");
        mappings.put("PROSPECTOS_LEADS_GOOGLE_PLACES_LANGUAGE_CODE", "prospectos.leads.google-places.language-code");
        mappings.put("PROSPECTOS_LEADS_GOOGLE_PLACES_MAX_RESULTS", "prospectos.leads.google-places.max-results");
        mappings.put("PROSPECTOS_LEADS_GOOGLE_PLACES_FIELD_MASK", "prospectos.leads.google-places.field-mask");
        mappings.put("OPENAI_API_KEY", "spring.ai.openai.api-key");
        mappings.put("ANTHROPIC_API_KEY", "spring.ai.anthropic.api-key");
        mappings.put("GROQ_API_KEY", "prospectos.ai.groq.api-key");
        mappings.put("SPRING_PROFILES_ACTIVE", "spring.profiles.active");
        mappings.put("SERVER_PORT", "server.port");
        mappings.put("DEBUG", "debug");
        mappings.put("DOTENV_DEBUG", "dotenv.debug");
        mappings.put("LOGGING_LEVEL_ROOT", "logging.level.root");
        mappings.put("LOGGING_LEVEL_DEV_PROSPECTOS", "logging.level.dev.prospectos");
        mappings.put("LOGGING_LEVEL_DEV_PROSPECTOS_AI", "logging.level.dev.prospectos.ai");
        mappings.put("LOGGING_LEVEL_DEV_PROSPECTOS_CONFIG", "logging.level.dev.prospectos.config");
        mappings.put("LOGGING_LEVEL_DEV_PROSPECTOS_AI_CONFIG", "logging.level.dev.prospectos.ai.config");
        mappings.put("LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_BOOT_AUTOCONFIGURE",
            "logging.level.org.springframework.boot.autoconfigure");
        mappings.put("LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_BOOT_CONTEXT_CONFIG",
            "logging.level.org.springframework.boot.context.config");
        mappings.put("LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_BEANS_FACTORY_SUPPORT",
            "logging.level.org.springframework.beans.factory.support");
        mappings.put("SPRING_DATASOURCE_URL", "spring.datasource.url");
        mappings.put("SPRING_DATASOURCE_DRIVER_CLASS_NAME", "spring.datasource.driver-class-name");
        mappings.put("SPRING_DATASOURCE_USERNAME", "spring.datasource.username");
        mappings.put("SPRING_DATASOURCE_PASSWORD", "spring.datasource.password");
        mappings.put("SPRING_H2_CONSOLE_ENABLED", "spring.h2.console.enabled");
        mappings.put("SPRING_JPA_DATABASE_PLATFORM", "spring.jpa.database-platform");
        mappings.put("SPRING_JPA_HIBERNATE_DDL_AUTO", "spring.jpa.hibernate.ddl-auto");
        mappings.put("SPRING_JPA_SHOW_SQL", "spring.jpa.show-sql");
        mappings.put("SCRAPER_SERVICE_URL", "scraper.service.url");
        mappings.put("SCRAPER_SERVICE_TIMEOUT", "scraper.service.timeout");
        mappings.put("NEWS_API_KEY", "news.api.key");
        mappings.put("NEWS_API_URL", "news.api.url");
        mappings.put("JWT_SECRET", "jwt.secret");
        mappings.put("JWT_EXPIRATION", "jwt.expiration");
        mappings.put("CORS_ALLOWED_ORIGINS", "cors.allowed.origins");
        mappings.put("CORS_ALLOWED_METHODS", "cors.allowed.methods");
        mappings.put("CORS_ALLOWED_HEADERS", "cors.allowed.headers");
        mappings.put("MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE", "management.endpoints.web.exposure.include");
        mappings.put("MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS", "management.endpoint.health.show-details");
        mappings.put("MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED", "management.metrics.export.prometheus.enabled");
        mappings.put("MOCK_AI_RESPONSES_ENABLED", "mock.ai.responses.enabled");
        mappings.put("MOCK_AI_RESPONSE_DELAY", "mock.ai.response.delay");
        mappings.put("AI_REQUEST_TIMEOUT", "ai.request.timeout");
        mappings.put("AI_MAX_RETRIES", "ai.max.retries");
        mappings.put("AI_PROVIDER_PRIORITY", "prospectos.ai.active-providers");
        mappings.put("FEATURE_AI_ENRICHMENT_ENABLED", "feature.ai.enrichment.enabled");
        mappings.put("FEATURE_AUTO_SCORING_ENABLED", "feature.auto.scoring.enabled");
        mappings.put("FEATURE_OUTREACH_GENERATION_ENABLED", "feature.outreach.generation.enabled");
        mappings.put("FEATURE_SIGNAL_DETECTION_ENABLED", "feature.signal.detection.enabled");
        mappings.put("SPRING_DEVTOOLS_RESTART_ENABLED", "spring.devtools.restart.enabled");
        mappings.put("SPRING_DEVTOOLS_LIVERELOAD_ENABLED", "spring.devtools.livereload.enabled");
        return mappings;
    }
}

