package dev.prospectos.ai.client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class LlmJsonTextCleaner {

    String removeReferencesSection(String value) {
        Matcher matcher = Pattern.compile("(?is)\\n\\s*references\\s*:.*$").matcher(value);
        return matcher.find() ? value.substring(0, matcher.start()) : value;
    }

    String removeLineComments(String value) {
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

    String normalizeControlCharactersInsideStrings(String json) {
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
                out.append(c == '\r' || c == '\n' || c == '\t' ? ' ' : c);
                continue;
            }
            if (c == '"') {
                inString = true;
            }
            out.append(c);
        }
        return out.toString();
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
}
