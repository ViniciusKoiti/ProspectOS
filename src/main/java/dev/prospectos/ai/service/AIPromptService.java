package dev.prospectos.ai.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import dev.prospectos.ai.exception.AIConfigurationException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AIPromptService {

    private static final String B2B_PROMPT_FILE = "prompts/b2b-prospecting.txt";
    private static final String SCORING_PROMPT_FILE = "prompts/scoring-system.txt";

    private final Map<PromptType, String> promptCache = new EnumMap<>(PromptType.class);

    @PostConstruct
    private void loadPrompts() {
        log.info("Loading AI prompts from resource files...");
        try {
            promptCache.put(PromptType.B2B_PROSPECTING, loadPromptFromFile(B2B_PROMPT_FILE));
            promptCache.put(PromptType.SCORING_SYSTEM, loadPromptFromFile(SCORING_PROMPT_FILE));
            log.info("Successfully loaded {} AI prompts", promptCache.size());
        } catch (Exception e) {
            log.error("Failed to load AI prompts: {}", e.getMessage(), e);
            throw new AIConfigurationException("prompts", "loading", "Failed to load system prompts", e);
        }
    }

    public String getB2BProspectingPrompt() {
        return getPrompt(PromptType.B2B_PROSPECTING);
    }

    public String getScoringPrompt() {
        return getPrompt(PromptType.SCORING_SYSTEM);
    }

    public String getPrompt(PromptType type) {
        String prompt = promptCache.get(type);
        if (prompt == null) {
            throw new AIConfigurationException("prompts", type.name().toLowerCase(), "Prompt not found: " + type);
        }
        return prompt;
    }

    public boolean arePromptsLoaded() {
        return promptCache.size() == PromptType.values().length;
    }

    public Map<PromptType, Integer> getPromptLengths() {
        Map<PromptType, Integer> lengths = new EnumMap<>(PromptType.class);
        promptCache.forEach((type, prompt) -> lengths.put(type, prompt.length()));
        return lengths;
    }

    private String loadPromptFromFile(String filename) {
        log.debug("Loading prompt from file: {}", filename);
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (inputStream == null) {
                throw new AIConfigurationException("prompts", "file-not-found", "Prompt file not found: " + filename);
            }
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            log.debug("Loaded prompt from {}: {} characters", filename, content.length());
            return content.trim();
        } catch (IOException e) {
            throw new AIConfigurationException("prompts", "io-error", "Failed to read prompt file: " + filename, e);
        }
    }

    public enum PromptType {
        B2B_PROSPECTING,
        SCORING_SYSTEM
    }
}
