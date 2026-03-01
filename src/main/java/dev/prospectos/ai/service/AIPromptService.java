package dev.prospectos.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import dev.prospectos.ai.exception.AIConfigurationException;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

/**
 * Service for managing AI system prompts.
 * Loads prompts from external files and provides centralized access.
 */
@Service
@Slf4j
public class AIPromptService {

    private final Map<PromptType, String> promptCache = new EnumMap<>(PromptType.class);

    @PostConstruct
    private void loadPrompts() {
        log.info("Loading AI prompts from resource files...");
        
        try {
            promptCache.put(PromptType.B2B_PROSPECTING, loadPromptFromFile("prompts/b2b-prospecting.txt"));
            promptCache.put(PromptType.SCORING_SYSTEM, loadPromptFromFile("prompts/scoring-system.txt"));
            
            log.info("✅ Successfully loaded {} AI prompts", promptCache.size());
        } catch (Exception e) {
            log.error("❌ Failed to load AI prompts: {}", e.getMessage(), e);
            throw new AIConfigurationException("prompts", "loading", "Failed to load system prompts", e);
        }
    }

    /**
     * Get the B2B prospecting system prompt.
     */
    public String getB2BProspectingPrompt() {
        return getPrompt(PromptType.B2B_PROSPECTING);
    }

    /**
     * Get the scoring system prompt.
     */
    public String getScoringPrompt() {
        return getPrompt(PromptType.SCORING_SYSTEM);
    }

    /**
     * Get a prompt by type.
     */
    public String getPrompt(PromptType type) {
        String prompt = promptCache.get(type);
        if (prompt == null) {
            throw new AIConfigurationException("prompts", type.name().toLowerCase(), 
                "Prompt not found: " + type);
        }
        return prompt;
    }

    /**
     * Check if all prompts are loaded successfully.
     */
    public boolean arePromptsLoaded() {
        return promptCache.size() == PromptType.values().length;
    }

    /**
     * Get information about loaded prompts for debugging.
     */
    public Map<PromptType, Integer> getPromptLengths() {
        Map<PromptType, Integer> lengths = new EnumMap<>(PromptType.class);
        promptCache.forEach((type, prompt) -> lengths.put(type, prompt.length()));
        return lengths;
    }

    private String loadPromptFromFile(String filename) {
        log.debug("Loading prompt from file: {}", filename);
        
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (inputStream == null) {
                throw new AIConfigurationException("prompts", "file-not-found", 
                    "Prompt file not found: " + filename);
            }
            
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            log.debug("Loaded prompt from {}: {} characters", filename, content.length());
            return content.trim();
            
        } catch (IOException e) {
            throw new AIConfigurationException("prompts", "io-error", 
                "Failed to read prompt file: " + filename, e);
        }
    }

    /**
     * Types of available prompts.
     */
    public enum PromptType {
        B2B_PROSPECTING,
        SCORING_SYSTEM
    }
}