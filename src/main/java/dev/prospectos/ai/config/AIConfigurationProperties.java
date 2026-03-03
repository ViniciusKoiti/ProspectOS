package dev.prospectos.ai.config;

/**
 * Constants for AI configuration properties.
 * Eliminates magic strings across AI configuration classes.
 */
public final class AIConfigurationProperties {
    
    // Base AI properties
    public static final String AI_ENABLED = "prospectos.ai.enabled";
    public static final String AI_PROVIDER = "prospectos.ai.provider";
    public static final String ACTIVE_PROVIDERS = "prospectos.ai.active-providers";
    
    // Groq provider properties
    public static final String GROQ_API_KEY = "prospectos.ai.groq.api-key";
    public static final String GROQ_BASE_URL = "prospectos.ai.groq.base-url";
    public static final String GROQ_MODEL = "prospectos.ai.groq.model";
    
    // OpenAI provider properties
    public static final String OPENAI_API_KEY = "spring.ai.openai.api-key";
    public static final String OPENAI_MODEL = "spring.ai.openai.chat.options.model";
    
    // Anthropic provider properties
    public static final String ANTHROPIC_API_KEY = "spring.ai.anthropic.api-key";
    public static final String ANTHROPIC_MODEL = "spring.ai.anthropic.chat.options.model";
    
    // Default values
    public static final String DEFAULT_ACTIVE_PROVIDERS = "openai,anthropic";
    public static final String DEFAULT_GROQ_BASE_URL = "https://api.groq.com/openai";
    public static final String DEFAULT_GROQ_MODEL = "llama3-8b-8192";
    public static final String DEFAULT_OPENAI_MODEL = "gpt-4-turbo-preview";
    public static final String DEFAULT_ANTHROPIC_MODEL = "claude-3-5-sonnet-20241022";
    
    // Profile exclusions
    public static final String EXCLUDE_TEST_PROFILE = "!test";
    
    private AIConfigurationProperties() {
        // Utility class - prevent instantiation
    }
}
