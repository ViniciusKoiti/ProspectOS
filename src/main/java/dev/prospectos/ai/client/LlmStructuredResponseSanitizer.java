package dev.prospectos.ai.client;

import org.springframework.stereotype.Component;

@Component
public class LlmStructuredResponseSanitizer {

    private final LlmJsonTextCleaner cleaner = new LlmJsonTextCleaner();

    public String preprocess(String raw) {
        if (raw == null) {
            return "";
        }
        String value = raw.trim();
        value = value.replace("```json", "").replace("```", "");
        value = cleaner.removeReferencesSection(value);
        value = cleaner.removeLineComments(value);
        value = value.replaceAll("\\[(\\d+)]", "");
        value = value.replaceAll(",\\s*([}\\]])", "$1");
        return value.trim();
    }

    public String extractFirstJsonObject(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        int start = text.indexOf('{');
        if (start < 0) {
            return null;
        }
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
            } else if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return text.substring(start, i + 1);
                }
            }
        }
        return null;
    }

    public String sanitizeJson(String json) {
        if (json == null) {
            return null;
        }
        String normalized = cleaner.normalizeControlCharactersInsideStrings(json);
        return normalized.replaceAll(",\\s*([}\\]])", "$1");
    }
}
