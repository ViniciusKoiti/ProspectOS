package dev.prospectos.ai.client;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared sanitizer for textual LLM responses that should contain JSON.
 */
@Component
public class LlmStructuredResponseSanitizer {

    public String preprocess(String raw) {
        if (raw == null) {
            return "";
        }

        String value = raw.trim();
        value = value.replace("```json", "").replace("```", "");
        value = removeReferencesSection(value);
        value = removeLineComments(value);
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

        String normalized = normalizeControlCharactersInsideStrings(json);
        normalized = normalized.replaceAll(",\\s*([}\\]])", "$1");
        return normalized;
    }

    private String removeReferencesSection(String value) {
        Matcher matcher = Pattern.compile("(?is)\\n\\s*references\\s*:.*$").matcher(value);
        return matcher.find() ? value.substring(0, matcher.start()) : value;
    }

    private String removeLineComments(String value) {
        String[] lines = value.split("\\R");
        StringBuilder result = new StringBuilder();
        for (String line : lines) {
            String stripped = stripLineComment(line);
            if (!stripped.isBlank()) {
                if (!result.isEmpty()) {
                    result.append('\n');
                }
                result.append(stripped);
            }
        }
        return result.toString();
    }

    private String stripLineComment(String line) {
        boolean inString = false;
        boolean escaped = false;
        for (int i = 0; i < line.length() - 1; i++) {
            char c = line.charAt(i);
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
                continue;
            }
            if (c == '/' && line.charAt(i + 1) == '/') {
                return line.substring(0, i).trim();
            }
        }
        return line.trim();
    }

    private String normalizeControlCharactersInsideStrings(String json) {
        StringBuilder out = new StringBuilder(json.length());
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                    out.append(c);
                    continue;
                }
                if (c == '\\') {
                    escaped = true;
                    out.append(c);
                    continue;
                }
                if (c == '"') {
                    inString = false;
                    out.append(c);
                    continue;
                }
                if (c == '\r' || c == '\n' || c == '\t') {
                    out.append(' ');
                } else {
                    out.append(c);
                }
                continue;
            }

            if (c == '"') {
                inString = true;
            }
            out.append(c);
        }

        return out.toString();
    }
}
