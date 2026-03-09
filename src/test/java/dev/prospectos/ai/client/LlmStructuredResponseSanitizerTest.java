package dev.prospectos.ai.client;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LlmStructuredResponseSanitizerTest {

    private final LlmStructuredResponseSanitizer sanitizer = new LlmStructuredResponseSanitizer();

    @Test
    void preprocessRemovesFencesCommentsAndReferences() {
        String input = """
            ```json
            {
              "score": 10, // comment
              "reasoning": "ok"
            }
            ```
            References:
            [1] https://example.com
            """;

        String preprocessed = sanitizer.preprocess(input);

        assertThat(preprocessed).doesNotContain("```");
        assertThat(preprocessed).doesNotContain("References:");
        assertThat(preprocessed).doesNotContain("// comment");
    }

    @Test
    void extractAndSanitizeJsonKeepsValidObject() {
        String input = "prefix {\"a\": \"x\\r\\n y\", \"b\": 1,} suffix";

        String extracted = sanitizer.extractFirstJsonObject(input);
        String sanitized = sanitizer.sanitizeJson(extracted);

        assertThat(extracted).startsWith("{").endsWith("}");
        assertThat(sanitized).doesNotContain(",}");
    }

    @Test
    void preprocessKeepsDoubleSlashInsideQuotedStrings() {
        String input = """
            {
              "url": "https://example.com/path",
              "note": "ok" // remove this comment
            }
            """;

        String preprocessed = sanitizer.preprocess(input);

        assertThat(preprocessed).contains("https://example.com/path");
        assertThat(preprocessed).doesNotContain("// remove this comment");
    }
}
