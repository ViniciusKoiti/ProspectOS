package dev.prospectos.ai.client;

import java.util.Arrays;
import java.util.List;

final class AIWebSearchNewsParser {

    private AIWebSearchNewsParser() {
    }

    static List<String> parse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(response.split("\n"))
            .map(String::trim)
            .filter(line -> !line.isEmpty())
            .filter(line -> !line.startsWith("Here") && !line.startsWith("I found"))
            .map(line -> line.replaceAll("^[-â€¢*]\\s*", ""))
            .filter(line -> line.length() > 10)
            .limit(10)
            .toList();
    }
}
