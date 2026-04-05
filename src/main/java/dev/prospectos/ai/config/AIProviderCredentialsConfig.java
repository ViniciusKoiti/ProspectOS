package dev.prospectos.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIProviderCredentialsConfig {

    @Bean
    public AIProviderCredentials aiProviderCredentials(
        @Value("${spring.ai.openai.api-key:}") String openaiKey,
        @Value("${spring.ai.anthropic.api-key:}") String anthropicKey,
        @Value("${prospectos.ai.groq.api-key:}") String groqKey
    ) {
        return new AIProviderCredentials(openaiKey, anthropicKey, groqKey);
    }
}
