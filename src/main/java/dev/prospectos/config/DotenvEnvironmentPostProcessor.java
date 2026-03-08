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
    private static final Map<String, String> ENV_TO_PROPERTY = buildEnvMappings();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (isTestExecution() || environment.acceptsProfiles(Profiles.of(TEST_PROFILE))) {
            return;
        }
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
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
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private boolean isTestExecution() {
        return System.getProperty("org.gradle.test.worker") != null
            || System.getProperty("surefire.test.class.path") != null;
    }

    private static Map<String, String> buildEnvMappings() {
        return DotenvEnvironmentMappings.ordered();
    }
}
