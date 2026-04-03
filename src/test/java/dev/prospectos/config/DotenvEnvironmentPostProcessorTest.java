package dev.prospectos.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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
    void shouldSkipWhenSurefirePropertyIsPresent() {
        ConfigurableEnvironment environment = new StandardEnvironment();

        withTemporarySystemProperties(
            Map.of("surefire.test.class.path", "classpath"),
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
        assertThat(mappings.get("PROSPECTOS_AI_ENABLED")).isEqualTo("prospectos.ai.enabled");
        assertThat(mappings.get("PROSPECTOS_LEADS_GOOGLE_PLACES_ENABLED"))
            .isEqualTo("prospectos.leads.google-places.enabled");
        assertThat(mappings.get("PROSPECTOS_LEADS_GOOGLE_PLACES_API_KEY"))
            .isEqualTo("prospectos.leads.google-places.api-key");
        assertThat(mappings.get("PROSPECTOS_PROSPECT_PAGESPEED_ENABLED"))
            .isEqualTo("prospectos.prospect.pagespeed.enabled");
        assertThat(mappings.get("PROSPECTOS_PROSPECT_PAGESPEED_API_KEY"))
            .isEqualTo("prospectos.prospect.pagespeed.api-key");
        assertThat(mappings.get("PROSPECTOS_PROSPECT_HUNTER_ENABLED"))
            .isEqualTo("prospectos.prospect.hunter.enabled");
        assertThat(mappings.get("PROSPECTOS_PROSPECT_HUNTER_API_KEY"))
            .isEqualTo("prospectos.prospect.hunter.api-key");
        assertThat(mappings.get("PROSPECTOS_OUTREACH_RESEND_ENABLED"))
            .isEqualTo("prospectos.outreach.resend.enabled");
        assertThat(mappings.get("PROSPECTOS_OUTREACH_RESEND_API_KEY"))
            .isEqualTo("prospectos.outreach.resend.api-key");
        assertThat(mappings.get("DEBUG")).isEqualTo("debug");
        assertThat(mappings.get("LOGGING_LEVEL_DEV_PROSPECTOS_AI_CONFIG"))
            .isEqualTo("logging.level.dev.prospectos.ai.config");
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
                "PROSPECTOS_AI_GROQ_API_KEY=groq-test-key",
                "PROSPECTOS_LEADS_GOOGLE_PLACES_ENABLED=true",
                "PROSPECTOS_LEADS_GOOGLE_PLACES_API_KEY=google-places-key",
                "PROSPECTOS_LEADS_GOOGLE_PLACES_LANGUAGE_CODE=en-US",
                "PROSPECTOS_LEADS_GOOGLE_PLACES_MAX_RESULTS=15",
                "PROSPECTOS_PROSPECT_PAGESPEED_ENABLED=true",
                "PROSPECTOS_PROSPECT_PAGESPEED_API_KEY=pagespeed-key",
                "PROSPECTOS_PROSPECT_PAGESPEED_STRATEGY=desktop",
                "PROSPECTOS_PROSPECT_PAGESPEED_LOCALE=en-US",
                "PROSPECTOS_PROSPECT_HUNTER_ENABLED=true",
                "PROSPECTOS_PROSPECT_HUNTER_API_KEY=hunter-key",
                "PROSPECTOS_PROSPECT_HUNTER_MAX_RESULTS=4",
                "PROSPECTOS_OUTREACH_RESEND_ENABLED=true",
                "PROSPECTOS_OUTREACH_RESEND_API_KEY=resend-key"
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
        assertThat(environment.getProperty("prospectos.leads.google-places.enabled")).isEqualTo("true");
        assertThat(environment.getProperty("prospectos.leads.google-places.api-key")).isEqualTo("google-places-key");
        assertThat(environment.getProperty("prospectos.leads.google-places.language-code")).isEqualTo("en-US");
        assertThat(environment.getProperty("prospectos.leads.google-places.max-results")).isEqualTo("15");
        assertThat(environment.getProperty("prospectos.prospect.pagespeed.enabled")).isEqualTo("true");
        assertThat(environment.getProperty("prospectos.prospect.pagespeed.api-key")).isEqualTo("pagespeed-key");
        assertThat(environment.getProperty("prospectos.prospect.pagespeed.strategy")).isEqualTo("desktop");
        assertThat(environment.getProperty("prospectos.prospect.pagespeed.locale")).isEqualTo("en-US");
        assertThat(environment.getProperty("prospectos.prospect.hunter.enabled")).isEqualTo("true");
        assertThat(environment.getProperty("prospectos.prospect.hunter.api-key")).isEqualTo("hunter-key");
        assertThat(environment.getProperty("prospectos.prospect.hunter.max-results")).isEqualTo("4");
        assertThat(environment.getProperty("prospectos.outreach.resend.enabled")).isEqualTo("true");
        assertThat(environment.getProperty("prospectos.outreach.resend.api-key")).isEqualTo("resend-key");
        assertThat(environment.getPropertySources().iterator().next().getName()).isEqualTo("dotenv");
    }

    @Test
    void shouldNotAddDotenvSourceWhenOnlyBlankValuesAreProvided() throws IOException {
        ConfigurableEnvironment environment = new StandardEnvironment();

        Map<String, String> properties = new HashMap<>();
        properties.put("org.gradle.test.worker", null);
        properties.put("surefire.test.class.path", null);

        withProjectDotenv(
            String.join(
                System.lineSeparator(),
                "SPRING_AI_OPENAI_API_KEY=",
                "SPRING_AI_ANTHROPIC_API_KEY=   "
            ) + System.lineSeparator(),
            () -> withTemporarySystemProperties(
                properties,
                () -> processor.postProcessEnvironment(environment, new SpringApplication())
            )
        );

        assertThat(environment.getPropertySources().contains("dotenv")).isFalse();
    }

    @Test
    void shouldPrintDiagnosticsWhenDotenvDebugEnabled() throws IOException {
        ConfigurableEnvironment environment = new StandardEnvironment();

        Map<String, String> properties = new HashMap<>();
        properties.put("org.gradle.test.worker", null);
        properties.put("surefire.test.class.path", null);

        String output = withProjectDotenv(
            String.join(
                System.lineSeparator(),
                "DOTENV_DEBUG=yes",
                "SPRING_PROFILES_ACTIVE=development",
                "PROSPECTOS_AI_ENABLED=true",
                "PROSPECTOS_AI_ACTIVE_PROVIDERS=groq,mock",
                "SCRAPER_AI_ENABLED=false",
                "SPRING_AI_OPENAI_API_KEY=openai-key"
            ) + System.lineSeparator(),
            () -> {
                String[] captured = new String[1];
                withTemporarySystemProperties(
                    properties,
                    () -> captured[0] = captureStdOut(
                        () -> processor.postProcessEnvironment(environment, new SpringApplication())
                    )
                );
                return captured[0];
            }
        );

        assertThat(output).contains("[dotenv] mapped properties loaded:");
        assertThat(output).contains("[dotenv] spring.profiles.active=development");
        assertThat(output).contains("[dotenv] prospectos.ai.active-providers=groq,mock");
        assertThat(output).contains("[dotenv] prospectos.ai.enabled=true");
        assertThat(output).contains("[dotenv] scraper.ai.enabled=false");
        assertThat(output).contains("api key presence (openai/groq/anthropic)=true/false/false");
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

    private static <T> T withProjectDotenv(String content, ThrowingSupplier<T> runnable) throws IOException {
        Path dotenvPath = Path.of(".env").toAbsolutePath().normalize();
        Path backupPath = null;

        if (Files.exists(dotenvPath)) {
            backupPath = Files.createTempFile(dotenvPath.getParent(), ".env-backup-", ".tmp");
            Files.copy(dotenvPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        }

        try {
            Files.writeString(dotenvPath, content, StandardCharsets.UTF_8);
            return runnable.get();
        } finally {
            if (backupPath != null) {
                Files.copy(backupPath, dotenvPath, StandardCopyOption.REPLACE_EXISTING);
                Files.deleteIfExists(backupPath);
            } else {
                Files.deleteIfExists(dotenvPath);
            }
        }
    }

    private static void withProjectDotenv(String content, ThrowingRunnable runnable) throws IOException {
        withProjectDotenv(content, () -> {
            runnable.run();
            return null;
        });
    }

    private static String captureStdOut(ThrowingRunnable runnable) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
        try {
            runnable.run();
            return output.toString(StandardCharsets.UTF_8);
        } finally {
            System.setOut(originalOut);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run();
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get();
    }
}
