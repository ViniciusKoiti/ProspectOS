package dev.prospectos.quality;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ClassLengthLintTest {

    private static final int TARGET_MAX_LINES = 50;
    private static final int HARD_MAX_LINES = 100;

    private static final Path PROJECT_ROOT = Path.of("").toAbsolutePath().normalize();
    private static final Path PRODUCTION_SOURCE_ROOT = PROJECT_ROOT.resolve("src/main/java");
    private static final Path ALLOWLIST_PATH =
        PROJECT_ROOT.resolve("src/test/resources/quality/class-length-allowlist.txt");

    @Test
    void productionClassesShouldRespectHardLimitOrBeExplicitlyAllowlisted() throws IOException {
        Set<String> allowlist = readAllowlist(ALLOWLIST_PATH);
        Map<String, Integer> filesAboveHardLimit = collectFilesAboveLimit(PRODUCTION_SOURCE_ROOT, HARD_MAX_LINES);

        List<String> nonAllowlistedViolations = filesAboveHardLimit.entrySet().stream()
            .filter(entry -> !allowlist.contains(entry.getKey()))
            .map(entry -> entry.getKey() + " (" + entry.getValue() + " lines)")
            .sorted()
            .toList();

        List<String> staleAllowlistEntries = allowlist.stream()
            .filter(path -> !filesAboveHardLimit.containsKey(path))
            .sorted()
            .toList();

        assertThat(nonAllowlistedViolations)
            .withFailMessage(
                """
                    New class-length violations (> %s lines) detected.
                    Target size is up to %s lines; acceptable hard limit is %s lines.
                    If unavoidable, register the file in %s.

                    Violations:
                    %s
                    """,
                HARD_MAX_LINES,
                TARGET_MAX_LINES,
                HARD_MAX_LINES,
                PROJECT_ROOT.relativize(ALLOWLIST_PATH).toString().replace('\\', '/'),
                String.join(System.lineSeparator(), nonAllowlistedViolations)
            )
            .isEmpty();

        assertThat(staleAllowlistEntries)
            .withFailMessage(
                """
                    Remove stale entries from class-length allowlist.
                    These files are now <= %s lines:

                    %s
                    """,
                HARD_MAX_LINES,
                String.join(System.lineSeparator(), staleAllowlistEntries)
            )
            .isEmpty();
    }

    private static Set<String> readAllowlist(Path allowlistPath) throws IOException {
        if (!Files.exists(allowlistPath)) {
            return Set.of();
        }
        return Files.readAllLines(allowlistPath).stream()
            .map(String::trim)
            .filter(line -> !line.isBlank())
            .filter(line -> !line.startsWith("#"))
            .collect(Collectors.toCollection(TreeSet::new));
    }

    private static Map<String, Integer> collectFilesAboveLimit(Path sourceRoot, int limit) throws IOException {
        try (Stream<Path> pathStream = Files.walk(sourceRoot)) {
            return pathStream
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .filter(path -> !path.getFileName().toString().equals("package-info.java"))
                .filter(path -> !path.getFileName().toString().equals("module-info.java"))
                .collect(
                    Collectors.toMap(
                        path -> toRepositoryRelativePath(path),
                        path -> countLines(path),
                        (left, right) -> left,
                        TreeMap::new
                    )
                )
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() > limit)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (left, right) -> left, TreeMap::new));
        }
    }

    private static int countLines(Path path) {
        try (Stream<String> lines = uncheckedLines(path)) {
            return (int) lines.count();
        }
    }

    private static Stream<String> uncheckedLines(Path path) {
        try {
            return Files.lines(path);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read file for class-length lint: " + path, ex);
        }
    }

    private static String toRepositoryRelativePath(Path absolutePath) {
        return PROJECT_ROOT.relativize(absolutePath.toAbsolutePath().normalize()).toString().replace('\\', '/');
    }
}
