package dev.prospectos.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.Profiles;

import java.util.HashMap;
import java.util.Map;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String TEST_PROFILE = "test";
    private static final String ACTIVE_PROFILE_PROPERTY = "spring.profiles.active";
    private static final String DOTENV_SOURCE_NAME = "dotenv";
    private static final String DOTENV_DEBUG_ENV = "DOTENV_DEBUG";
    private static final Map<String, String> ENV_TO_PROPERTY = buildEnvMappings();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (isTestExecution() || environment.acceptsProfiles(Profiles.of(TEST_PROFILE))) {
            return;
        }
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        boolean dotenvDebug = isTrue(dotenv.get(DOTENV_DEBUG_ENV, "false"));
        Map<String, Object> props = new HashMap<>();
        for (Map.Entry<String, String> mapping : ENV_TO_PROPERTY.entrySet()) {
            String propertyKey = mapping.getValue();
            if (ACTIVE_PROFILE_PROPERTY.equals(propertyKey) && environment.getProperty(ACTIVE_PROFILE_PROPERTY) != null) {
                continue;
            }
            String value = dotenv.get(mapping.getKey(), null);
            if (value != null && !value.isBlank()) {
                props.putIfAbsent(propertyKey, value);
            }
        }
        if (!props.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(DOTENV_SOURCE_NAME, props));
        }
        if (dotenvDebug) {
            printDiagnostics(props, environment);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private boolean isTestExecution() {
        return System.getProperty("org.gradle.test.worker") != null
            || System.getProperty("surefire.test.class.path") != null;
    }

    private boolean isTrue(String value) {
        return "true".equalsIgnoreCase(value) || "1".equals(value) || "yes".equalsIgnoreCase(value);
    }

    private void printDiagnostics(Map<String, Object> props, ConfigurableEnvironment environment) {
        String profiles = props.getOrDefault(ACTIVE_PROFILE_PROPERTY, environment.getProperty(ACTIVE_PROFILE_PROPERTY, "<unset>"))
            .toString();
        String providers = props.getOrDefault("prospectos.ai.active-providers",
            environment.getProperty("prospectos.ai.active-providers", "<unset>")).toString();
        String aiEnabled = props.getOrDefault("prospectos.ai.enabled",
            environment.getProperty("prospectos.ai.enabled", "<unset>")).toString();
        String scraperAiEnabled = props.getOrDefault("scraper.ai.enabled",
            environment.getProperty("scraper.ai.enabled", "<unset>")).toString();

        boolean openAiKeySet = hasValue(props, "spring.ai.openai.api-key");
        boolean groqKeySet = hasValue(props, "prospectos.ai.groq.api-key");
        boolean anthropicKeySet = hasValue(props, "spring.ai.anthropic.api-key");

        System.out.println("[dotenv] mapped properties loaded: " + props.size());
        System.out.println("[dotenv] spring.profiles.active=" + profiles);
        System.out.println("[dotenv] prospectos.ai.enabled=" + aiEnabled);
        System.out.println("[dotenv] prospectos.ai.active-providers=" + providers);
        System.out.println("[dotenv] scraper.ai.enabled=" + scraperAiEnabled);
        System.out.println("[dotenv] api key presence (openai/groq/anthropic)="
            + openAiKeySet + "/" + groqKeySet + "/" + anthropicKeySet);
    }

    private boolean hasValue(Map<String, Object> props, String key) {
        Object value = props.get(key);
        return value != null && !value.toString().isBlank();
    }

    private static Map<String, String> buildEnvMappings() {
        return DotenvEnvironmentMappings.ordered();
    }
}
