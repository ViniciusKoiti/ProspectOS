package dev.prospectos.core.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoreTest {

    @Test
    void ofRejectsNullOrOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> Score.of((BigDecimal) null));
        assertThrows(IllegalArgumentException.class, () -> Score.of(-1));
        assertThrows(IllegalArgumentException.class, () -> Score.of(101));
    }

    @Test
    void categoryThresholdsAreApplied() {
        assertEquals(Score.ScoreCategory.HIGH, Score.of(80).getCategory());
        assertEquals(Score.ScoreCategory.MEDIUM, Score.of(50).getCategory());
        assertEquals(Score.ScoreCategory.LOW, Score.of(20).getCategory());
        assertEquals(Score.ScoreCategory.VERY_LOW, Score.of(19.99).getCategory());
    }

    @Test
    void addCapsAtMaximum() {
        Score score = Score.of(90).add(Score.of(20));
        assertEquals(100.0, score.getDoubleValue());
    }

    @Test
    void multiplyCapsAndRejectsNegativeFactors() {
        assertThrows(IllegalArgumentException.class, () -> Score.of(10).multiply(new BigDecimal("-1")));

        Score score = Score.of(80).multiply(new BigDecimal("2"));
        assertEquals(100.0, score.getDoubleValue());
        assertTrue(score.isHighPriority());
    }
}
