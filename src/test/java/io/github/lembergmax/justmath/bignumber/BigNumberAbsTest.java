package io.github.lembergmax.justmath.bignumber;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BigNumberAbsTest {

    @Test
    public void absOfPositiveIsUnchangedTest() {
        assertEquals("5", new BigNumber("5", Locale.GERMAN).abs().toString());
    }

    @Test
    public void absOfNegativeIsPositiveTest() {
        assertEquals("5", new BigNumber("-5", Locale.GERMAN).abs().toString());
    }

    @Test
    public void absOfNegativeIsNotNegativeTest() {
        assertFalse(new BigNumber("-5", Locale.GERMAN).abs().isNegative());
    }

    @Test
    public void absOfZeroIsNotNegativeTest() {
        assertFalse(new BigNumber("0", Locale.GERMAN).abs().isNegative());
    }

    @Test
    public void absOfNegativeZeroIsNotNegativeTest() {
        assertFalse(new BigNumber("-0", Locale.GERMAN).abs().isNegative());
    }

    @Test
    public void absOfDecimalValueTest() {
        assertEquals("1,25", new BigNumber("-1,25", Locale.GERMAN).abs().toString());
    }

    @Test
    public void absDoesNotChangeOperandTest() {
        final BigNumber negative = new BigNumber("-5", Locale.GERMAN);

        negative.abs();

        assertTrue(negative.isNegative());
    }

}
