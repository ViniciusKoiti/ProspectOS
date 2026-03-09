package dev.prospectos.ai.client;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
final class LlmScrapingResponsePreprocessor {
    String preprocess(String response) {
        if (response == null) {
            return "";
        }
        String value = response.trim();
        value = value.replace("```json", "").replace("```", "");
        value = removeReferencesSection(value);
        value = removeLineComments(value);
        value = value.replaceAll("\\[(\\d+)]", "");
        value = value.replaceAll("\"\\s*\\(([^)]*)\\)", " ($1)\"");
        value = value.replaceAll(",\\s*([}\\]])", "$1");
        return value.trim();
    }
    String extractJsonFromResponse(String response) {
        if (response == null || response.isBlank()) {
            return null;
        }
        return extractFirstBalancedJsonObject(response);
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
    private String extractFirstBalancedJsonObject(String value) {
        int start = value.indexOf('{');
        if (start < 0) {
            return null;
        }
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int i = start; i < value.length(); i++) {
            char c = value.charAt(i);
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
                    return value.substring(start, i + 1);
                }
            }
        }
        return null;
    }
}
