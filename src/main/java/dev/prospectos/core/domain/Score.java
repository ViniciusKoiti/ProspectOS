package dev.prospectos.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Embeddable
public final class Score {
    private static final BigDecimal MIN_SCORE = BigDecimal.ZERO;
    private static final BigDecimal MAX_SCORE = new BigDecimal("100.0");

    @Column(name = "prospecting_score")
    private final BigDecimal value;

    protected Score() {
        this.value = MIN_SCORE;
    }

    private Score(BigDecimal value) {
        this.value = value.setScale(2, RoundingMode.HALF_UP);
    }

    public static Score of(double value) {
        return of(BigDecimal.valueOf(value));
    }

    public static Score of(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("Score value cannot be null");
        }
        if (value.compareTo(MIN_SCORE) < 0 || value.compareTo(MAX_SCORE) > 0) {
            throw new IllegalArgumentException(String.format("Score must be between %s and %s, but was: %s", MIN_SCORE, MAX_SCORE, value));
        }
        return new Score(value);
    }

    public static Score zero() { return new Score(MIN_SCORE); }
    public static Score max() { return new Score(MAX_SCORE); }
    public BigDecimal getValue() { return value; }
    public double getDoubleValue() { return value.doubleValue(); }

    public ScoreCategory getCategory() {
        if (value.compareTo(new BigDecimal("80")) >= 0) {
            return ScoreCategory.HIGH;
        }
        if (value.compareTo(new BigDecimal("50")) >= 0) {
            return ScoreCategory.MEDIUM;
        }
        if (value.compareTo(new BigDecimal("20")) >= 0) {
            return ScoreCategory.LOW;
        }
        return ScoreCategory.VERY_LOW;
    }

    public boolean isHighPriority() { return getCategory() == ScoreCategory.HIGH; }
    public boolean isAboveThreshold(Score threshold) { return value.compareTo(threshold.value) >= 0; }

    public Score add(Score other) {
        return new Score(cap(value.add(other.value)));
    }

    public Score multiply(BigDecimal factor) {
        if (factor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Factor cannot be negative");
        }
        return new Score(cap(value.multiply(factor)));
    }

    private BigDecimal cap(BigDecimal score) {
        return score.compareTo(MAX_SCORE) > 0 ? MAX_SCORE : score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Score score = (Score) o;
        return Objects.equals(value, score.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }
    @Override
    public String toString() { return value.toString(); }

    public enum ScoreCategory {
        VERY_LOW("Very Low"), LOW("Low"), MEDIUM("Medium"), HIGH("High");
        private final String description;

        ScoreCategory(String description) { this.description = description; }
        public String getDescription() { return description; }
    }
}
