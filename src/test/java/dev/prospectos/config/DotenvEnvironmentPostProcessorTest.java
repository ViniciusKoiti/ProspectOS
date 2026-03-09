package dev.prospectos.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DotenvEnvironmentPostProcessorTest {

    private final DotenvEnvironmentPostProcessor processor = new DotenvEnvironmentPostProcessor();

    @Test
    void shouldUseHighestPrecedenceOrder() {
        assertThat(processor.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
    }

    @Test
    void shouldSkipWhenTestProfileIsActive() {
        ConfigurableEnvironment environment = new StandardEnvironment();
        environment.addActiveProfile("test");

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getPropertySources().contains("dotenv")).isFalse();
    }

    @Test
    void shouldSkipWhenGradleTestWorkerPropertyIsPresent() {
        ConfigurableEnvironment environment = new StandardEnvironment();

        withTemporarySystemProperties(
            Map.of("org.gradle.test.worker", "worker-1"),
            () -> processor.postProcessEnvironment(environment, new SpringApplication())
        );

        assertThat(environment.getPropertySources().contains("dotenv")).isFalse();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldKeepExpectedEnvironmentMappings() throws Exception {
        Method method = DotenvEnvironmentPostProcessor.class.getDeclaredMethod("buildEnvMappings");
        method.setAccessible(true);
        Map<String, String> mappings = (Map<String, String>) method.invoke(null);

        assertThat(mappings.get("OPENAI_API_KEY")).isEqualTo("spring.ai.openai.api-key");
        assertThat(mappings.get("ANTHROPIC_API_KEY")).isEqualTo("spring.ai.anthropic.api-key");
        assertThat(mappings.get("AI_PROVIDER_PRIORITY")).isEqualTo("prospectos.ai.active-providers");
        assertThat(mappings.get("PROSPECTOS_AI_GROQ_API_KEY")).isEqualTo("prospectos.ai.groq.api-key");
    }

    @Test
    void shouldLoadMappedValuesFromDotenvAndNotOverrideExistingSpringProfile() throws IOException {
        ConfigurableEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(
            new MapPropertySource("existing", Map.of("spring.profiles.active", "development"))
        );

        Map<String, String> properties = new HashMap<>();
        properties.put("org.gradle.test.worker", null);
        properties.put("surefire.test.class.path", null);
        withProjectDotenv(
            String.join(
                System.lineSeparator(),
                "SPRING_PROFILES_ACTIVE=mock",
                "SERVER_PORT=9090",
                "AI_PROVIDER_PRIORITY=mock,openai",
                "PROSPECTOS_AI_GROQ_API_KEY=groq-test-key"
            ) + System.lineSeparator(),
            () -> withTemporarySystemProperties(
                properties,
                () -> processor.postProcessEnvironment(environment, new SpringApplication())
            )
        );

        assertThat(environment.getPropertySources().contains("dotenv")).isTrue();
        assertThat(environment.getProperty("spring.profiles.active")).isEqualTo("development");
        assertThat(environment.getProperty("server.port")).isEqualTo("9090");
        assertThat(environment.getProperty("prospectos.ai.active-providers")).isEqualTo("mock,openai");
        assertThat(environment.getProperty("prospectos.ai.groq.api-key")).isEqualTo("groq-test-key");
        assertThat(environment.getPropertySources().iterator().next().getName()).isEqualTo("dotenv");
    }

    private static void withTemporarySystemProperties(Map<String, String> overrides, ThrowingRunnable runnable) {
        Map<String, String> previousValues = new HashMap<>();
        for (Map.Entry<String, String> entry : overrides.entrySet()) {
            String key = entry.getKey();
            previousValues.put(key, System.getProperty(key));
            if (entry.getValue() == null) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, entry.getValue());
            }
        }

        try {
            runnable.run();
        } finally {
            for (Map.Entry<String, String> entry : previousValues.entrySet()) {
                if (entry.getValue() == null) {
                    System.clearProperty(entry.getKey());
                } else {
                    System.setProperty(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private static void withProjectDotenv(String content, ThrowingRunnable runnable) throws IOException {
        Path dotenvPath = Path.of(".env").toAbsolutePath().normalize();
        Path backupPath = null;

        if (Files.exists(dotenvPath)) {
            backupPath = Files.createTempFile(dotenvPath.getParent(), ".env-backup-", ".tmp");
            Files.copy(dotenvPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        }

        try {
            Files.writeString(dotenvPath, content, StandardCharsets.UTF_8);
            runnable.run();
        } finally {
            if (backupPath != null) {
                Files.copy(backupPath, dotenvPath, StandardCopyOption.REPLACE_EXISTING);
                Files.deleteIfExists(backupPath);
            } else {
                Files.deleteIfExists(dotenvPath);
            }
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run();
    }
}
