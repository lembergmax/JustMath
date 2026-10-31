package io.github.lembergmax.justmath.bignumber;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class BigNumberGreaterThanTest {

    @Test
    public void greaterThanFourDigitsSmallerNumberIsFalseTest() {
        assertFalse(new BigNumber("2415", Locale.GERMAN).isGreaterThan(new BigNumber("3276", Locale.GERMAN)));
    }

    @Test
    public void greaterThanThreeDigitsLargerNumberIsTrueTest() {
        assertTrue(new BigNumber("321", Locale.GERMAN).isGreaterThan(new BigNumber("314", Locale.GERMAN)));
    }

    @Test
    public void greaterThanLongerNumberAgainstShorterNumberIsTrueTest() {
        assertTrue(german("1000").isGreaterThan(german("999")));
    }

    @Test
    public void greaterThanShorterNumberAgainstLongerNumberIsFalseTest() {
        assertFalse(german("999").isGreaterThan(german("1000")));
    }

    @Test
    public void greaterThanTwoDigitsAgainstOneDigitIsTrueTest() {
        assertTrue(german("10").isGreaterThan(german("9")));
    }

    @Test
    public void greaterThanFewerDecimalsButLargerValueIsTrueTest() {
        assertTrue(german("1,5").isGreaterThan(german("1,25")));
    }

    @Test
    public void greaterThanMoreDecimalsButSmallerValueIsFalseTest() {
        assertFalse(german("1,25").isGreaterThan(german("1,5")));
    }

    @Test
    public void greaterThanLargerLastDecimalIsTrueTest() {
        assertTrue(german("1,0002").isGreaterThan(german("1,0001")));
    }

    @Test
    public void greaterThanSmallerLastDecimalIsFalseTest() {
        assertFalse(german("1,0001").isGreaterThan(german("1,0002")));
    }

    @Test
    public void greaterThanAgainstLeadingZeroInDecimalsIsTrueTest() {
        assertTrue(german("0,5").isGreaterThan(german("0,05")));
    }

    @Test
    public void greaterThanLeadingZeroInDecimalsIsFalseTest() {
        assertFalse(german("0,05").isGreaterThan(german("0,5")));
    }

    @Test
    public void greaterThanIntegerAgainstSmallerDecimalIsTrueTest() {
        assertTrue(german("2").isGreaterThan(german("1,999")));
    }

    @Test
    public void greaterThanDecimalAgainstLargerIntegerIsFalseTest() {
        assertFalse(german("1,999").isGreaterThan(german("2")));
    }

    // Gleiche Werte sind weder größer noch kleiner
    @Test
    public void greaterThanEqualIntegersIsFalseTest() {
        assertFalse(german("5").isGreaterThan(german("5")));
    }

    @Test
    public void greaterThanEqualDecimalsIsFalseTest() {
        assertFalse(german("123,456").isGreaterThan(german("123,456")));
    }

    @Test
    public void greaterThanEqualWithTrailingZeroIsFalseTest() {
        assertFalse(german("1,50").isGreaterThan(german("1,5")));
    }

    @Test
    public void greaterThanEqualWithLeadingZerosIsFalseTest() {
        assertFalse(german("007").isGreaterThan(german("7")));
    }

    @Test
    public void greaterThanEqualWithPositiveSignIsFalseTest() {
        assertFalse(german("+5").isGreaterThan(german("5")));
    }

    @Test
    public void greaterThanEqualWithGroupingSeparatorIsFalseTest() {
        assertFalse(german("1.000").isGreaterThan(german("1000")));
    }

    @Test
    public void greaterThanEqualNegativeIntegersIsFalseTest() {
        assertFalse(german("-5").isGreaterThan(german("-5")));
    }

    @Test
    public void greaterThanEqualNegativeWithTrailingZeroIsFalseTest() {
        assertFalse(german("-1,5").isGreaterThan(german("-1,50")));
    }

    @Test
    public void greaterThanZeroAgainstZeroIsFalseTest() {
        assertFalse(german("0").isGreaterThan(german("0")));
    }

    @Test
    public void greaterThanPositiveAgainstZeroIsTrueTest() {
        assertTrue(german("5").isGreaterThan(german("0")));
    }

    @Test
    public void greaterThanZeroAgainstPositiveIsFalseTest() {
        assertFalse(german("0").isGreaterThan(german("5")));
    }

    @Test
    public void greaterThanSmallDecimalAgainstZeroIsTrueTest() {
        assertTrue(german("0,0001").isGreaterThan(german("0")));
    }

    @Test
    public void greaterThanZeroAgainstSmallDecimalIsFalseTest() {
        assertFalse(german("0").isGreaterThan(german("0,0001")));
    }

    // -0 hat denselben Wert wie 0
    @Test
    public void greaterThanNegativeZeroAgainstZeroIsFalseTest() {
        assertFalse(german("-0").isGreaterThan(german("0")));
    }

    @Test
    public void greaterThanZeroAgainstNegativeZeroIsFalseTest() {
        assertFalse(german("0").isGreaterThan(german("-0")));
    }

    @Test
    public void greaterThanNegativeZeroAgainstNegativeZeroIsFalseTest() {
        assertFalse(german("-0").isGreaterThan(german("-0")));
    }

    @Test
    public void greaterThanNegativeZeroAgainstNegativeIsTrueTest() {
        assertTrue(german("-0").isGreaterThan(german("-5")));
    }

    @Test
    public void greaterThanNegativeAgainstNegativeZeroIsFalseTest() {
        assertFalse(german("-5").isGreaterThan(german("-0")));
    }

    @Test
    public void greaterThanNegativeZeroAgainstPositiveIsFalseTest() {
        assertFalse(german("-0").isGreaterThan(german("5")));
    }

    @Test
    public void greaterThanPositiveAgainstNegativeZeroIsTrueTest() {
        assertTrue(german("5").isGreaterThan(german("-0")));
    }

    // + -
    @Test
    public void greaterThanPositiveWithLargerAbsoluteAgainstNegativeIsTrueTest() {
        assertTrue(german("5").isGreaterThan(german("-3")));
    }

    @Test
    public void greaterThanPositiveWithSmallerAbsoluteAgainstNegativeIsTrueTest() {
        assertTrue(german("3").isGreaterThan(german("-5")));
    }

    @Test
    public void greaterThanNegativeWithLargerAbsoluteAgainstPositiveIsFalseTest() {
        assertFalse(german("-5").isGreaterThan(german("3")));
    }

    @Test
    public void greaterThanNegativeWithSmallerAbsoluteAgainstPositiveIsFalseTest() {
        assertFalse(german("-3").isGreaterThan(german("5")));
    }

    @Test
    public void greaterThanPositiveAgainstNegativeWithSameAbsoluteIsTrueTest() {
        assertTrue(german("5").isGreaterThan(german("-5")));
    }

    @Test
    public void greaterThanNegativeAgainstPositiveWithSameAbsoluteIsFalseTest() {
        assertFalse(german("-5").isGreaterThan(german("5")));
    }

    @Test
    public void greaterThanZeroAgainstNegativeDecimalIsTrueTest() {
        assertTrue(german("0").isGreaterThan(german("-0,1")));
    }

    @Test
    public void greaterThanNegativeDecimalAgainstZeroIsFalseTest() {
        assertFalse(german("-0,1").isGreaterThan(german("0")));
    }

    // - -
    @Test
    public void greaterThanBothNegativeSmallerAbsoluteIsTrueTest() {
        assertTrue(german("-3").isGreaterThan(german("-5")));
    }

    @Test
    public void greaterThanBothNegativeLargerAbsoluteIsFalseTest() {
        assertFalse(german("-5").isGreaterThan(german("-3")));
    }

    @Test
    public void greaterThanBothNegativeShorterNumberIsTrueTest() {
        assertTrue(german("-999").isGreaterThan(german("-1000")));
    }

    @Test
    public void greaterThanBothNegativeLongerNumberIsFalseTest() {
        assertFalse(german("-1000").isGreaterThan(german("-999")));
    }

    @Test
    public void greaterThanBothNegativeSmallerAbsoluteDecimalIsTrueTest() {
        assertTrue(german("-1,25").isGreaterThan(german("-1,5")));
    }

    @Test
    public void greaterThanBothNegativeLargerAbsoluteDecimalIsFalseTest() {
        assertFalse(german("-1,5").isGreaterThan(german("-1,25")));
    }

    @Test
    public void greaterThanBothNegativeLeadingZeroInDecimalsIsTrueTest() {
        assertTrue(german("-0,05").isGreaterThan(german("-0,5")));
    }

    @Test
    public void greaterThanBothNegativeAgainstLeadingZeroInDecimalsIsFalseTest() {
        assertFalse(german("-0,5").isGreaterThan(german("-0,05")));
    }

    @Test
    public void greaterThanVeryLargeLargerValueIsTrueTest() {
        assertTrue(german("1000000000000000000000000000001").isGreaterThan(german("1000000000000000000000000000000")));
    }

    @Test
    public void greaterThanVeryLargeSmallerValueIsFalseTest() {
        assertFalse(german("1000000000000000000000000000000").isGreaterThan(german("1000000000000000000000000000001")));
    }

    @Test
    public void greaterThanVeryLargeNegativeSmallerAbsoluteIsTrueTest() {
        assertTrue(german("-1000000000000000000000000000000").isGreaterThan(german("-1000000000000000000000000000001")));
    }

    @Test
    public void greaterThanVeryLargeNegativeLargerAbsoluteIsFalseTest() {
        assertFalse(german("-1000000000000000000000000000001").isGreaterThan(german("-1000000000000000000000000000000")));
    }

    @Test
    public void greaterThanEnglishLocaleLargerDecimalIsTrueTest() {
        assertTrue(english("1.5").isGreaterThan(english("1.25")));
    }

    @Test
    public void greaterThanEnglishLocaleSmallerDecimalIsFalseTest() {
        assertFalse(english("1.25").isGreaterThan(english("1.5")));
    }

    @Test
    public void greaterThanEnglishLocaleEqualDecimalsIsFalseTest() {
        assertFalse(english("1.5").isGreaterThan(english("1.5")));
    }

    @Test
    public void greaterThanEnglishLocaleWithGroupingSeparatorIsTrueTest() {
        assertTrue(english("1,000").isGreaterThan(english("999")));
    }

    @Test
    public void greaterThanEnglishLocaleBothNegativeSmallerAbsoluteIsTrueTest() {
        assertTrue(english("-1.25").isGreaterThan(english("-1.5")));
    }

    @Test
    public void greaterThanEnglishLocaleBothNegativeLargerAbsoluteIsFalseTest() {
        assertFalse(english("-1.5").isGreaterThan(english("-1.25")));
    }

    @Test
    public void greaterThanDifferentLocalesEqualValuesIsFalseTest() {
        assertFalse(german("2,5").isGreaterThan(english("2.5")));
    }

    @Test
    public void greaterThanDifferentLocalesGermanLargerIsTrueTest() {
        assertTrue(german("2,5").isGreaterThan(english("2.25")));
    }

    @Test
    public void greaterThanDifferentLocalesEnglishSmallerIsFalseTest() {
        assertFalse(english("2.25").isGreaterThan(german("2,5")));
    }

    @Test
    public void greaterThanDifferentLocalesBothNegativeSmallerAbsoluteIsTrueTest() {
        assertTrue(german("-3,5").isGreaterThan(english("-4.5")));
    }

    @Test
    public void greaterThanDifferentLocalesBothNegativeLargerAbsoluteIsFalseTest() {
        assertFalse(german("-4,5").isGreaterThan(english("-3.5")));
    }

    private static BigNumber german(final String number) {
        return new BigNumber(number, Locale.GERMAN);
    }

    private static BigNumber english(final String number) {
        return new BigNumber(number, Locale.US);
    }
}
