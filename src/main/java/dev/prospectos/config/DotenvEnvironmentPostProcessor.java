package dev.prospectos.config;


import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        Map<String, Object> props = new HashMap<>();

        // Carrega chaves do .env e disponibiliza pro Spring Environment
        // Use os nomes CANÔNICOS do Spring AI (mais confiável)
        String openai = dotenv.get("OPENAI_API_KEY", null);
        if (openai != null && !openai.isBlank()) {
            props.put("spring.ai.openai.api-key", openai);
        }

        String anthropic = dotenv.get("ANTHROPIC_API_KEY", null);
        if (anthropic != null && !anthropic.isBlank()) {
            props.put("spring.ai.anthropic.api-key", anthropic);
        }

        if (!props.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource("dotenv", props));
        }
    }

    // Alta prioridade (roda cedo)
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
