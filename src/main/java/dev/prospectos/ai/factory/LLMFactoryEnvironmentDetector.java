package dev.prospectos.ai.factory;

import java.util.Arrays;

import org.springframework.core.env.Environment;

final class LLMFactoryEnvironmentDetector {

    private LLMFactoryEnvironmentDetector() {
    }

    static boolean isTestEnvironment(Environment environment) {
        return Arrays.stream(environment.getActiveProfiles())
            .anyMatch(profile -> profile.contains("test"))
            || environment.getProperty("spring.profiles.active", "").contains("test")
            || isRunningInTestContext();
    }

    private static boolean isRunningInTestContext() {
        return Arrays.stream(Thread.currentThread().getStackTrace())
            .anyMatch(element -> element.getClassName().contains("junit")
                || element.getClassName().contains("Test")
                || element.getClassName().contains("test"));
    }
}
