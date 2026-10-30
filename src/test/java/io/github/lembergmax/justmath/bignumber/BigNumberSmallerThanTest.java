package io.github.lembergmax.justmath.bignumber;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class BigNumberSmallerThanTest {

    @Test
    public void smallerThanFourDigitsSmallerNumberIsTrueTest() {
        assertTrue(new BigNumber("2425", Locale.GERMAN).smallerThan(new BigNumber("3276", Locale.GERMAN)));
    }

    @Test
    public void smallerThanThreeDigitsLargerNumberIsFalseTest() {
        assertFalse(new BigNumber("321", Locale.GERMAN).smallerThan(new BigNumber("314", Locale.GERMAN)));
    }

    @Test
    public void smallerThanShorterNumberAgainstLongerNumberIsTrueTest() {
        assertTrue(german("999").smallerThan(german("1000")));
    }

    @Test
    public void smallerThanLongerNumberAgainstShorterNumberIsFalseTest() {
        assertFalse(german("1000").smallerThan(german("999")));
    }

    @Test
    public void smallerThanOneDigitAgainstTwoDigitsIsTrueTest() {
        assertTrue(german("9").smallerThan(german("10")));
    }

    @Test
    public void smallerThanMoreDecimalsButSmallerValueIsTrueTest() {
        assertTrue(german("1,25").smallerThan(german("1,5")));
    }

    @Test
    public void smallerThanFewerDecimalsButLargerValueIsFalseTest() {
        assertFalse(german("1,5").smallerThan(german("1,25")));
    }

    @Test
    public void smallerThanSmallerLastDecimalIsTrueTest() {
        assertTrue(german("1,0001").smallerThan(german("1,0002")));
    }

    @Test
    public void smallerThanLargerLastDecimalIsFalseTest() {
        assertFalse(german("1,0002").smallerThan(german("1,0001")));
    }

    @Test
    public void smallerThanLeadingZeroInDecimalsIsTrueTest() {
        assertTrue(german("0,05").smallerThan(german("0,5")));
    }

    @Test
    public void smallerThanAgainstLeadingZeroInDecimalsIsFalseTest() {
        assertFalse(german("0,5").smallerThan(german("0,05")));
    }

    @Test
    public void smallerThanDecimalAgainstLargerIntegerIsTrueTest() {
        assertTrue(german("1,999").smallerThan(german("2")));
    }

    @Test
    public void smallerThanIntegerAgainstSmallerDecimalIsFalseTest() {
        assertFalse(german("2").smallerThan(german("1,999")));
    }

    // Gleiche Werte sind weder größer noch kleiner
    @Test
    public void smallerThanEqualIntegersIsFalseTest() {
        assertFalse(german("5").smallerThan(german("5")));
    }

    @Test
    public void smallerThanEqualDecimalsIsFalseTest() {
        assertFalse(german("123,456").smallerThan(german("123,456")));
    }

    @Test
    public void smallerThanEqualWithTrailingZeroIsFalseTest() {
        assertFalse(german("1,50").smallerThan(german("1,5")));
    }

    @Test
    public void smallerThanEqualWithLeadingZerosIsFalseTest() {
        assertFalse(german("007").smallerThan(german("7")));
    }

    @Test
    public void smallerThanEqualWithPositiveSignIsFalseTest() {
        assertFalse(german("+5").smallerThan(german("5")));
    }

    @Test
    public void smallerThanEqualWithGroupingSeparatorIsFalseTest() {
        assertFalse(german("1.000").smallerThan(german("1000")));
    }

    @Test
    public void smallerThanEqualNegativeIntegersIsFalseTest() {
        assertFalse(german("-5").smallerThan(german("-5")));
    }

    @Test
    public void smallerThanEqualNegativeWithTrailingZeroIsFalseTest() {
        assertFalse(german("-1,5").smallerThan(german("-1,50")));
    }

    @Test
    public void smallerThanZeroAgainstZeroIsFalseTest() {
        assertFalse(german("0").smallerThan(german("0")));
    }

    @Test
    public void smallerThanZeroAgainstPositiveIsTrueTest() {
        assertTrue(german("0").smallerThan(german("5")));
    }

    @Test
    public void smallerThanPositiveAgainstZeroIsFalseTest() {
        assertFalse(german("5").smallerThan(german("0")));
    }

    @Test
    public void smallerThanZeroAgainstSmallDecimalIsTrueTest() {
        assertTrue(german("0").smallerThan(german("0,0001")));
    }

    @Test
    public void smallerThanSmallDecimalAgainstZeroIsFalseTest() {
        assertFalse(german("0,0001").smallerThan(german("0")));
    }

    // -0 hat denselben Wert wie 0
    @Test
    public void smallerThanNegativeZeroAgainstZeroIsFalseTest() {
        assertFalse(german("-0").smallerThan(german("0")));
    }

    @Test
    public void smallerThanZeroAgainstNegativeZeroIsFalseTest() {
        assertFalse(german("0").smallerThan(german("-0")));
    }

    @Test
    public void smallerThanNegativeZeroAgainstNegativeZeroIsFalseTest() {
        assertFalse(german("-0").smallerThan(german("-0")));
    }

    @Test
    public void smallerThanNegativeAgainstNegativeZeroIsTrueTest() {
        assertTrue(german("-5").smallerThan(german("-0")));
    }

    @Test
    public void smallerThanNegativeZeroAgainstNegativeIsFalseTest() {
        assertFalse(german("-0").smallerThan(german("-5")));
    }

    @Test
    public void smallerThanNegativeZeroAgainstPositiveIsTrueTest() {
        assertTrue(german("-0").smallerThan(german("5")));
    }

    @Test
    public void smallerThanPositiveAgainstNegativeZeroIsFalseTest() {
        assertFalse(german("5").smallerThan(german("-0")));
    }

    // + -
    @Test
    public void smallerThanNegativeWithSmallerAbsoluteAgainstPositiveIsTrueTest() {
        assertTrue(german("-3").smallerThan(german("5")));
    }

    @Test
    public void smallerThanNegativeWithLargerAbsoluteAgainstPositiveIsTrueTest() {
        assertTrue(german("-5").smallerThan(german("3")));
    }

    @Test
    public void smallerThanPositiveWithSmallerAbsoluteAgainstNegativeIsFalseTest() {
        assertFalse(german("3").smallerThan(german("-5")));
    }

    @Test
    public void smallerThanPositiveWithLargerAbsoluteAgainstNegativeIsFalseTest() {
        assertFalse(german("5").smallerThan(german("-3")));
    }

    @Test
    public void smallerThanNegativeAgainstPositiveWithSameAbsoluteIsTrueTest() {
        assertTrue(german("-5").smallerThan(german("5")));
    }

    @Test
    public void smallerThanPositiveAgainstNegativeWithSameAbsoluteIsFalseTest() {
        assertFalse(german("5").smallerThan(german("-5")));
    }

    @Test
    public void smallerThanNegativeDecimalAgainstZeroIsTrueTest() {
        assertTrue(german("-0,1").smallerThan(german("0")));
    }

    @Test
    public void smallerThanZeroAgainstNegativeDecimalIsFalseTest() {
        assertFalse(german("0").smallerThan(german("-0,1")));
    }

    // - -
    @Test
    public void smallerThanBothNegativeLargerAbsoluteIsTrueTest() {
        assertTrue(german("-5").smallerThan(german("-3")));
    }

    @Test
    public void smallerThanBothNegativeSmallerAbsoluteIsFalseTest() {
        assertFalse(german("-3").smallerThan(german("-5")));
    }

    @Test
    public void smallerThanBothNegativeLongerNumberIsTrueTest() {
        assertTrue(german("-1000").smallerThan(german("-999")));
    }

    @Test
    public void smallerThanBothNegativeShorterNumberIsFalseTest() {
        assertFalse(german("-999").smallerThan(german("-1000")));
    }

    @Test
    public void smallerThanBothNegativeLargerAbsoluteDecimalIsTrueTest() {
        assertTrue(german("-1,5").smallerThan(german("-1,25")));
    }

    @Test
    public void smallerThanBothNegativeSmallerAbsoluteDecimalIsFalseTest() {
        assertFalse(german("-1,25").smallerThan(german("-1,5")));
    }

    @Test
    public void smallerThanBothNegativeAgainstLeadingZeroInDecimalsIsTrueTest() {
        assertTrue(german("-0,5").smallerThan(german("-0,05")));
    }

    @Test
    public void smallerThanBothNegativeLeadingZeroInDecimalsIsFalseTest() {
        assertFalse(german("-0,05").smallerThan(german("-0,5")));
    }

    @Test
    public void smallerThanVeryLargeSmallerValueIsTrueTest() {
        assertTrue(german("1000000000000000000000000000000").smallerThan(german("1000000000000000000000000000001")));
    }

    @Test
    public void smallerThanVeryLargeLargerValueIsFalseTest() {
        assertFalse(german("1000000000000000000000000000001").smallerThan(german("1000000000000000000000000000000")));
    }

    @Test
    public void smallerThanVeryLargeNegativeLargerAbsoluteIsTrueTest() {
        assertTrue(german("-1000000000000000000000000000001").smallerThan(german("-1000000000000000000000000000000")));
    }

    @Test
    public void smallerThanVeryLargeNegativeSmallerAbsoluteIsFalseTest() {
        assertFalse(german("-1000000000000000000000000000000").smallerThan(german("-1000000000000000000000000000001")));
    }

    @Test
    public void smallerThanEnglishLocaleSmallerDecimalIsTrueTest() {
        assertTrue(english("1.25").smallerThan(english("1.5")));
    }

    @Test
    public void smallerThanEnglishLocaleLargerDecimalIsFalseTest() {
        assertFalse(english("1.5").smallerThan(english("1.25")));
    }

    @Test
    public void smallerThanEnglishLocaleEqualDecimalsIsFalseTest() {
        assertFalse(english("1.5").smallerThan(english("1.5")));
    }

    @Test
    public void smallerThanEnglishLocaleWithGroupingSeparatorIsTrueTest() {
        assertTrue(english("999").smallerThan(english("1,000")));
    }

    @Test
    public void smallerThanEnglishLocaleBothNegativeLargerAbsoluteIsTrueTest() {
        assertTrue(english("-1.5").smallerThan(english("-1.25")));
    }

    @Test
    public void smallerThanEnglishLocaleBothNegativeSmallerAbsoluteIsFalseTest() {
        assertFalse(english("-1.25").smallerThan(english("-1.5")));
    }

    @Test
    public void smallerThanDifferentLocalesEqualValuesIsFalseTest() {
        assertFalse(german("2,5").smallerThan(english("2.5")));
    }

    @Test
    public void smallerThanDifferentLocalesEnglishSmallerIsTrueTest() {
        assertTrue(english("2.25").smallerThan(german("2,5")));
    }

    @Test
    public void smallerThanDifferentLocalesGermanLargerIsFalseTest() {
        assertFalse(german("2,5").smallerThan(english("2.25")));
    }

    @Test
    public void smallerThanDifferentLocalesBothNegativeLargerAbsoluteIsTrueTest() {
        assertTrue(german("-4,5").smallerThan(english("-3.5")));
    }

    @Test
    public void smallerThanDifferentLocalesBothNegativeSmallerAbsoluteIsFalseTest() {
        assertFalse(german("-3,5").smallerThan(english("-4.5")));
    }

    // Ein Vergleich darf die Operanden nicht verändern
    @Test
    public void smallerThanDoesNotChangeOperandsTest() {
        final BigNumber negativeZero = german("-0");

        negativeZero.smallerThan(german("-5"));

        assertTrue(negativeZero.isNegative());
    }

    private static BigNumber german(final String number) {
        return new BigNumber(number, Locale.GERMAN);
    }

    private static BigNumber english(final String number) {
        return new BigNumber(number, Locale.US);
    }
}
