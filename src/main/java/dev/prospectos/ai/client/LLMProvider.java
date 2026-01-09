package dev.prospectos.ai.client;

/**
 * Enum dos provedores LLM suportados
 */
public enum LLMProvider {
    OPENAI("OpenAI GPT-4", "Best overall quality"),
    ANTHROPIC("Claude 3.5 Sonnet", "Best complex analysis"),
    GROQ("Groq Llama 3", "Fast hosted inference"),
    OLLAMA("Ollama Local", "Free, local execution"),
    MOCK("Mock Provider", "For testing");
    
    private final String displayName;
    private final String description;
    
    LLMProvider(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
