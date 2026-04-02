package dev.prospectos.api.mcp;

import java.time.Duration;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public enum QueryTimeWindow {

    ONE_HOUR("1h", Duration.ofHours(1)),
    TWENTY_FOUR_HOURS("24h", Duration.ofHours(24)),
    SEVEN_DAYS("7d", Duration.ofDays(7)),
    THIRTY_DAYS("30d", Duration.ofDays(30));

    private final String value;
    private final Duration duration;

    QueryTimeWindow(String value, Duration duration) {
        this.value = value;
        this.duration = duration;
    }

    public String value() {
        return value;
    }

    public Duration duration() {
        return duration;
    }

    public static QueryTimeWindow fromValue(String value) {
        var normalized = normalize(value);
        return Arrays.stream(values())
            .filter(candidate -> candidate.value.equals(normalized))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Invalid timeWindow '%s'. Allowed values: %s".formatted(value, supportedValues())
            ));
    }

    public static String supportedValues() {
        return Arrays.stream(values())
            .map(QueryTimeWindow::value)
            .collect(Collectors.joining(", "));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
