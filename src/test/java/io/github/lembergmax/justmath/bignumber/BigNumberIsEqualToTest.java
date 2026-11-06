package io.github.lembergmax.justmath.bignumber;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BigNumberIsEqualToTest {
    @Test
    public void isEqualToSameIntegersIsTrueTest() {
        assertTrue(german("5").isEqualTo(german("5")));
    }

    @Test
    public void isEqualToSameDecimalsIsTrueTest() {
        assertTrue(german("2,5").isEqualTo(german("2,5")));
    }

    @Test
    public void isEqualToWithTrailingZeroIsTrueTest() {
        assertTrue(german("1,5").isEqualTo(german("1,50")));
    }

    @Test
    public void isEqualToWithLeadingZerosIsTrueTest() {
        assertTrue(german("007").isEqualTo(german("7")));
    }

    @Test
    public void isEqualToWithPositiveSignIsTrueTest() {
        assertTrue(german("+5").isEqualTo(german("5")));
    }

    @Test
    public void isEqualToWithGroupingSeparatorIsTrueTest() {
        assertTrue(german("1.000").isEqualTo(german("1000")));
    }

    @Test
    public void isEqualToIntegerAndDecimalWithZerosIsTrueTest() {
        assertTrue(german("5").isEqualTo(german("5,000")));
    }

    @Test
    public void isEqualToSameNegativeIntegersIsTrueTest() {
        assertTrue(german("-5").isEqualTo(german("-5")));
    }

    @Test
    public void isEqualToSameNegativeWithTrailingZeroIsTrueTest() {
        assertTrue(german("-1,5").isEqualTo(german("-1,50")));
    }

    @Test
    public void isEqualToSameVeryLargeValuesIsTrueTest() {
        assertTrue(german("90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000").isEqualTo(german("90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000")));
    }

    @Test
    public void isEqualToSameVerySmallDecimalsIsTrueTest() {
        assertTrue(german("0,00000001").isEqualTo(german("0,00000001")));
    }

    // Ungleiche Werte
    @Test
    public void isEqualToLargerIntegerIsFalseTest() {
        assertFalse(german("6").isEqualTo(german("5")));
    }

    @Test
    public void isEqualToSmallerIntegerIsFalseTest() {
        assertFalse(german("5").isEqualTo(german("6")));
    }

    @Test
    public void isEqualToDifferentFirstDigitIsFalseTest() {
        assertFalse(german("2415").isEqualTo(german("3276")));
    }

    @Test
    public void isEqualToDifferentLastDigitIsFalseTest() {
        assertFalse(german("12345").isEqualTo(german("12346")));
    }

    @Test
    public void isEqualToDifferentLastDecimalIsFalseTest() {
        assertFalse(german("1,25").isEqualTo(german("1,26")));
    }

    @Test
    public void isEqualToMoreDecimalsIsFalseTest() {
        assertFalse(german("1,25").isEqualTo(german("1,251")));
    }

    @Test
    public void isEqualToShorterNumberIsFalseTest() {
        assertFalse(german("12").isEqualTo(german("123")));
    }

    @Test
    public void isEqualToSameDigitsDifferentDecimalPositionIsFalseTest() {
        assertFalse(german("12,3").isEqualTo(german("1,23")));
    }

    @Test
    public void isEqualToPositiveAgainstNegativeWithSameAbsoluteIsFalseTest() {
        assertFalse(german("5").isEqualTo(german("-5")));
    }

    @Test
    public void isEqualToNegativeAgainstPositiveWithSameAbsoluteIsFalseTest() {
        assertFalse(german("-5").isEqualTo(german("5")));
    }

    @Test
    public void isEqualToDifferentNegativeValuesIsFalseTest() {
        assertFalse(german("-5").isEqualTo(german("-6")));
    }

    @Test
    public void isEqualToVeryLargeDifferentLastDigitIsFalseTest() {
        assertFalse(german("90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000").isEqualTo(german("90000000000000000000000000000000000000000000000000000000000000000000000000000000000000001")));
    }

    @Test
    public void isEqualToVerySmallDecimalAgainstZeroIsFalseTest() {
        assertFalse(german("0,00000001").isEqualTo(german("0")));
    }

    @Test
    public void isEqualToZeroAgainstVerySmallDecimalIsFalseTest() {
        assertFalse(german("0").isEqualTo(german("0,00000001")));
    }

    // -0 hat denselben Wert wie 0
    @Test
    public void isEqualToZeroAgainstZeroIsTrueTest() {
        assertTrue(german("0").isEqualTo(german("0")));
    }

    @Test
    public void isEqualToNegativeZeroAgainstZeroIsTrueTest() {
        assertTrue(german("-0").isEqualTo(german("0")));
    }

    @Test
    public void isEqualToZeroAgainstNegativeZeroIsTrueTest() {
        assertTrue(german("0").isEqualTo(german("-0")));
    }

    @Test
    public void isEqualToNegativeZeroAgainstNegativeZeroIsTrueTest() {
        assertTrue(german("-0").isEqualTo(german("-0")));
    }

    @Test
    public void isEqualToZeroWithDecimalsAgainstZeroIsTrueTest() {
        assertTrue(german("0,000").isEqualTo(german("0")));
    }

    // Auch eine mit negate() erzeugte 0 ist gleich 0
    @Test
    public void isEqualToNegatedZeroAgainstZeroIsTrueTest() {
        assertTrue(german("0").negate().isEqualTo(german("0")));
    }

    @Test
    public void isEqualToZeroAgainstNegatedZeroIsTrueTest() {
        assertTrue(german("0").isEqualTo(german("0").negate()));
    }

    @Test
    public void isEqualToNegatedZeroAgainstNegativeZeroIsTrueTest() {
        assertTrue(german("0").negate().isEqualTo(german("-0")));
    }

    @Test
    public void isEqualToNegatedZeroAgainstNegatedZeroIsTrueTest() {
        assertTrue(german("0").negate().isEqualTo(german("0").negate()));
    }

    @Test
    public void isEqualToNegatedPositiveAgainstNegativeIsTrueTest() {
        assertTrue(german("5").negate().isEqualTo(german("-5")));
    }

    @Test
    public void isEqualToNegatedNegativeAgainstPositiveIsTrueTest() {
        assertTrue(german("-5").negate().isEqualTo(german("5")));
    }

    // Locale
    @Test
    public void isEqualToDifferentLocalesSameValueIsTrueTest() {
        assertTrue(german("2,5").isEqualTo(english("2.5")));
    }

    @Test
    public void isEqualToDifferentLocalesDifferentValueIsFalseTest() {
        assertFalse(german("2,5").isEqualTo(english("2.25")));
    }

    @Test
    public void isEqualToDifferentLocalesSameNegativeValueIsTrueTest() {
        assertTrue(german("-2,5").isEqualTo(english("-2.5")));
    }

    @Test
    public void isEqualToEnglishWithGroupingSeparatorAgainstGermanIsTrueTest() {
        assertTrue(english("1,000.5").isEqualTo(german("1000,5")));
    }

    // Eingabewerte dürfen nicht verändert werden
    @Test
    public void isEqualToDoesNotChangeOperandsTest() {
        final BigNumber first = german("-2,5");
        final BigNumber second = german("-2,50");

        first.isEqualTo(second);

        assertEquals("-2,5", first.toString(Locale.GERMAN));
        assertEquals("-2,5", second.toString(Locale.GERMAN));
    }

    private static BigNumber german(final String number) {
        return new BigNumber(number, Locale.GERMAN);
    }

    private static BigNumber english(final String number) {
        return new BigNumber(number, Locale.US);
    }

}
