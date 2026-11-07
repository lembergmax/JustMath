package io.github.lembergmax.justmath.core;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.exception.DuplicateDecimalSeparatorException;
import io.github.lembergmax.justmath.exception.NotAValidCharacterException;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class BigNumberParserTest {

    // Deutsch ohne Komma
    @Test
    public void germanEmptyStringIsZeroTest() {
        assertEquals("0", new BigNumber("", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void germanOneDigitTest() {
        assertEquals("1", new BigNumber("1", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void germanTwoDigitsTest() {
        assertEquals("12", new BigNumber("12", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void germanThreeDigitsTest() {
        assertEquals("123", new BigNumber("123", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void germanFourDigitsTest() {
        assertEquals("1234", new BigNumber("1234", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void germanFourDigitsWithGroupingSeparatorTest() {
        assertEquals("1234", new BigNumber("1.234", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void germanFiveDigitsTest() {
        assertEquals("12345", new BigNumber("12345", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void germanFiveDigitsWithGroupingSeparatorTest() {
        assertEquals("12345", new BigNumber("12.345", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void germanSixDigitsWithGroupingSeparatorTest() {
        assertEquals("123456", new BigNumber("123.456", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void germanSixDigitsTest() {
        assertEquals("123456", new BigNumber("123456", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void germanSevenDigitsTest() {
        assertEquals("1234567", new BigNumber("1234567", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void germanSevenDigitsWithGroupingSeparatorsTest() {
        assertEquals("1234567", new BigNumber("1.234.567", Locale.GERMAN).toString(Locale.GERMAN));
    }

    // Deutsch mit Komma
    @Test
    public void germanFourDecimalPlacesTest() {
        assertEquals("0,0001", new BigNumber("0,0001", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void germanThreeDecimalPlacesTest() {
        assertEquals("0,001", new BigNumber("0,001", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void germanTwoDecimalPlacesTest() {
        assertEquals("0,01", new BigNumber("0,01", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void germanOneDecimalPlaceTest() {
        assertEquals("0,1", new BigNumber("0,1", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void germanOneDigitWithOneDecimalPlaceTest() {
        assertEquals("1,1", new BigNumber("1,1", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void germanTwoDigitsWithTwoDecimalPlacesTest() {
        assertEquals("12,12", new BigNumber("12,12", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void germanThreeDigitsWithThreeDecimalPlacesTest() {
        assertEquals("123,123", new BigNumber("123,123", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void germanFourDigitsWithFourDecimalPlacesTest() {
        assertEquals("1234,1234", new BigNumber("1234,1234", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void germanFourDigitsWithGroupingSeparatorAndFourDecimalPlacesTest() {
        assertEquals("1234,1234", new BigNumber("1.234,1234", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void germanFiveDigitsWithFiveDecimalPlacesTest() {
        assertEquals("12345,12345", new BigNumber("12345,12345", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void germanFiveDigitsWithGroupingSeparatorAndFiveDecimalPlacesTest() {
        assertEquals("12345,12345", new BigNumber("12.345,12345", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void germanSixDigitsWithGroupingSeparatorAndSixDecimalPlacesTest() {
        assertEquals("123456,123456", new BigNumber("123.456,123456", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void germanSixDigitsWithSixDecimalPlacesTest() {
        assertEquals("123456,123456", new BigNumber("123456,123456", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void germanSevenDigitsWithSevenDecimalPlacesTest() {
        assertEquals("1234567,1234567", new BigNumber("1234567,1234567", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void germanSevenDigitsWithGroupingSeparatorsAndSevenDecimalPlacesTest() {
        assertEquals("1234567,1234567", new BigNumber("1.234.567,1234567", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void germanElevenDigitsTest() {
        assertEquals("50000000000", new BigNumber("50000000000", Locale.GERMAN).toString(Locale.GERMAN));
    }

    // Englisch ohne Komma
    //assertEquals("0",                   new BigNumber("", Locale.ENGLISH).toString(Locale.ENGLISH));
    @Test
    public void englishOneDigitTest() {
        assertEquals("1", new BigNumber("1", Locale.ENGLISH).toString(Locale.ENGLISH));
    }

    @Test
    public void englishTwoDigitsTest() {
        assertEquals("12", new BigNumber("12", Locale.ENGLISH).toString(Locale.ENGLISH));
    }

    @Test
    public void englishThreeDigitsTest() {
        assertEquals("123", new BigNumber("123", Locale.ENGLISH).toString(Locale.ENGLISH));
    }

    @Test
    public void englishFourDigitsTest() {
        assertEquals("1234", new BigNumber("1234", Locale.ENGLISH).toString(Locale.ENGLISH));
    }

    @Test
    public void englishFourDigitsWithGroupingSeparatorTest() {
        assertEquals("1234", new BigNumber("1,234", Locale.ENGLISH).toString(Locale.ENGLISH));
    }

    @Test
    public void englishFiveDigitsTest() {
        assertEquals("12345", new BigNumber("12345", Locale.ENGLISH).toString(Locale.ENGLISH));
    }

    @Test
    public void englishFiveDigitsWithGroupingSeparatorTest() {
        assertEquals("12345", new BigNumber("12,345", Locale.ENGLISH).toString(Locale.ENGLISH));
    }

    @Test
    public void englishSixDigitsWithGroupingSeparatorTest() {
        assertEquals("123456", new BigNumber("123,456", Locale.ENGLISH).toString(Locale.ENGLISH));
    }

    @Test
    public void englishSixDigitsTest() {
        assertEquals("123456", new BigNumber("123456", Locale.ENGLISH).toString(Locale.ENGLISH));
    }

    @Test
    public void englishSevenDigitsTest() {
        assertEquals("1234567", new BigNumber("1234567", Locale.ENGLISH).toString(Locale.ENGLISH));
    }

    @Test
    public void englishSevenDigitsWithGroupingSeparatorsTest() {
        assertEquals("1234567", new BigNumber("1,234,567", Locale.ENGLISH).toString(Locale.ENGLISH));
    }

    // Englisch mit Komma
    @Test
    public void englishFourDecimalPlacesTest() {
        assertEquals("0.0001", new BigNumber("0.0001", Locale.ENGLISH).toString(Locale.ENGLISH));
    }

    @Test
    public void englishThreeDecimalPlacesTest() {
        assertEquals("0.001", new BigNumber("0.001", Locale.ENGLISH).toString(Locale.ENGLISH));
    }

    @Test
    public void englishTwoDecimalPlacesTest() {
        assertEquals("0.01", new BigNumber("0.01", Locale.ENGLISH).toString(Locale.ENGLISH));
    }

    @Test
    public void englishOneDecimalPlaceTest() {
        assertEquals("0.1", new BigNumber("0.1", Locale.ENGLISH).toString(Locale.ENGLISH));
    }

    @Test
    public void englishOneDigitWithOneDecimalPlaceTest() {
        assertEquals("1.1", new BigNumber("1.1", Locale.ENGLISH).toString(Locale.ENGLISH));
    }

    @Test
    public void englishTwoDigitsWithTwoDecimalPlacesTest() {
        assertEquals("12.12", new BigNumber("12.12", Locale.ENGLISH).toString(Locale.ENGLISH));
    }

    @Test
    public void englishThreeDigitsWithThreeDecimalPlacesTest() {
        assertEquals("123.123", new BigNumber("123.123", Locale.ENGLISH).toString(Locale.ENGLISH));
    }

    @Test
    public void englishFourDigitsWithFourDecimalPlacesTest() {
        assertEquals("1234.1234", new BigNumber("1234.1234", Locale.ENGLISH).toString(Locale.ENGLISH));
    }

    @Test
    public void englishFourDigitsWithGroupingSeparatorAndFourDecimalPlacesTest() {
        assertEquals("1234.1234", new BigNumber("1,234.1234", Locale.ENGLISH).toString(Locale.ENGLISH));
    }

    @Test
    public void englishFiveDigitsWithFiveDecimalPlacesTest() {
        assertEquals("12345.12345", new BigNumber("12345.12345", Locale.ENGLISH).toString(Locale.ENGLISH));
    }

    @Test
    public void englishFiveDigitsWithGroupingSeparatorAndFiveDecimalPlacesTest() {
        assertEquals("12345.12345", new BigNumber("12,345.12345", Locale.ENGLISH).toString(Locale.ENGLISH));
    }

    @Test
    public void englishSixDigitsWithGroupingSeparatorAndSixDecimalPlacesTest() {
        assertEquals("123456.123456", new BigNumber("123,456.123456", Locale.ENGLISH).toString(Locale.ENGLISH));
    }

    @Test
    public void englishSixDigitsWithSixDecimalPlacesTest() {
        assertEquals("123456.123456", new BigNumber("123456.123456", Locale.ENGLISH).toString(Locale.ENGLISH));
    }

    @Test
    public void englishSevenDigitsWithSevenDecimalPlacesTest() {
        assertEquals("1234567.1234567", new BigNumber("1234567.1234567", Locale.ENGLISH).toString(Locale.ENGLISH));
    }

    @Test
    public void englishSevenDigitsWithGroupingSeparatorsAndSevenDecimalPlacesTest() {
        assertEquals("1234567.1234567", new BigNumber("1,234,567.1234567", Locale.ENGLISH).toString(Locale.ENGLISH));
    }

    @Test
    public void englishElevenDigitsTest() {
        assertEquals("50000000000", new BigNumber("50000000000", Locale.ENGLISH).toString(Locale.ENGLISH));
    }

    // Führende und abschließende Nullen
    @Test
    public void leadingZerosAreRemovedTest() {
        assertEquals("1", new BigNumber("00000000000001", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void smallDecimalWithManyLeadingZerosIsKeptTest() {
        assertEquals("0,000000000001", new BigNumber("0,000000000001", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void onlyZerosAfterDecimalSeparatorIsZeroTest() {
        assertEquals("0", new BigNumber("0,000000000000", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void onlyZerosBeforeAndAfterDecimalSeparatorIsZeroTest() {
        assertEquals("0", new BigNumber("000000,0000000", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void onlyZerosWithoutDecimalSeparatorIsZeroTest() {
        assertEquals("0", new BigNumber("00000000000000", Locale.GERMAN).toString(Locale.GERMAN));
    }

    // Nur Trennzeichen
    @Test
    public void onlyDecimalSeparatorIsZeroTest() {
        assertEquals("0", new BigNumber(",", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void onlyGroupingSeparatorIsZeroTest() {
        assertEquals("0", new BigNumber(".", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void decimalSeparatorFollowedByGroupingSeparatorIsZeroTest() {
        assertEquals("0", new BigNumber(",.", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void groupingSeparatorFollowedByDecimalSeparatorIsZeroTest() {
        assertEquals("0", new BigNumber(".,", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void decimalSeparatorBetweenGroupingSeparatorsIsZeroTest() {
        assertEquals("0", new BigNumber(".,.", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void onlyGroupingSeparatorsIsZeroTest() {
        assertEquals("0", new BigNumber("...", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void twoDecimalSeparatorsFollowedByGroupingSeparatorThrowsTest() {
        assertThrows(DuplicateDecimalSeparatorException.class, () -> new BigNumber(",,.", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void groupingSeparatorFollowedByTwoDecimalSeparatorsThrowsTest() {
        assertThrows(DuplicateDecimalSeparatorException.class, () -> new BigNumber(".,,", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void threeDecimalSeparatorsThrowsTest() {
        assertThrows(DuplicateDecimalSeparatorException.class, () -> new BigNumber(",,,", Locale.GERMAN).toString(Locale.GERMAN));
    }

    // Vorzeichen
    @Test
    public void isNegativePositiveZeroIsFalseTest() {
        assertFalse(new BigNumber("+0", Locale.GERMAN).isNegative());
    }

    @Test
    public void isNegativeNegativeZeroIsFalseTest() {
        assertFalse(new BigNumber("-0", Locale.GERMAN).isNegative());
    }

    @Test
    public void isNegativePositiveSmallDecimalIsFalseTest() {
        assertFalse(new BigNumber("+0,00000000001", Locale.GERMAN).isNegative());
    }

    @Test
    public void isNegativeLargeNumberWithoutSignIsFalseTest() {
        assertFalse(new BigNumber("10000000", Locale.GERMAN).isNegative());
    }

    @Test
    public void isNegativeOneIsFalseTest() {
        assertFalse(new BigNumber("1", Locale.GERMAN).isNegative());
    }

    @Test
    public void isNegativeMinusOneIsTrueTest() {
        assertTrue(new BigNumber("-1", Locale.GERMAN).isNegative());
    }

    @Test
    public void isNegativeTenIsFalseTest() {
        assertFalse(new BigNumber("10", Locale.GERMAN).isNegative());
    }

    @Test
    public void isNegativeMinusTenIsTrueTest() {
        assertTrue(new BigNumber("-10", Locale.GERMAN).isNegative());
    }

    @Test
    public void isNegativeNegativeWithLeadingZeroIsTrueTest() {
        assertTrue(new BigNumber("-01", Locale.GERMAN).isNegative());
    }

    @Test
    public void isNegativeNegativeWithLeadingZerosIsTrueTest() {
        assertTrue(new BigNumber("-001", Locale.GERMAN).isNegative());
    }

    @Test
    public void isNegativePositiveWithLeadingZeroIsFalseTest() {
        assertFalse(new BigNumber("+01", Locale.GERMAN).isNegative());
    }

    @Test
    public void isNegativePositiveWithLeadingZerosIsFalseTest() {
        assertFalse(new BigNumber("+001", Locale.GERMAN).isNegative());
    }

    // Ungültige Zeichen
    @Test
    public void onlyLettersThrowsTest() {
        assertThrows(NotAValidCharacterException.class, () -> new BigNumber("abc", Locale.GERMAN));
    }

    @Test
    public void letterAtEndThrowsTest() {
        assertThrows(NotAValidCharacterException.class, () -> new BigNumber("12a", Locale.GERMAN));
    }

    @Test
    public void letterAtStartThrowsTest() {
        assertThrows(NotAValidCharacterException.class, () -> new BigNumber("a12", Locale.GERMAN));
    }

    @Test
    public void letterInMiddleThrowsTest() {
        assertThrows(NotAValidCharacterException.class, () -> new BigNumber("1a2", Locale.GERMAN));
    }

    @Test
    public void letterInDecimalsThrowsTest() {
        assertThrows(NotAValidCharacterException.class, () -> new BigNumber("5,5a", Locale.GERMAN));
    }

    @Test
    public void currencySignThrowsTest() {
        assertThrows(NotAValidCharacterException.class, () -> new BigNumber("5€", Locale.GERMAN));
    }

    @Test
    public void percentSignThrowsTest() {
        assertThrows(NotAValidCharacterException.class, () -> new BigNumber("5%", Locale.GERMAN));
    }

    @Test
    public void slashThrowsTest() {
        assertThrows(NotAValidCharacterException.class, () -> new BigNumber("1/2", Locale.GERMAN));
    }

    @Test
    public void asteriskThrowsTest() {
        assertThrows(NotAValidCharacterException.class, () -> new BigNumber("2*3", Locale.GERMAN));
    }

    @Test
    public void scientificNotationThrowsTest() {
        assertThrows(NotAValidCharacterException.class, () -> new BigNumber("1e5", Locale.GERMAN));
    }

    @Test
    public void apostropheInGermanThrowsTest() {
        assertThrows(NotAValidCharacterException.class, () -> new BigNumber("1'000", Locale.GERMAN));
    }

    @Test
    public void englishLetterThrowsTest() {
        assertThrows(NotAValidCharacterException.class, () -> new BigNumber("1x", Locale.US));
    }

    @Test
    public void invalidCharacterMessageNamesCharacterTest() {
        final NotAValidCharacterException exception = assertThrows(
                NotAValidCharacterException.class,
                () -> new BigNumber("12a", Locale.GERMAN)
        );

        assertEquals("'a' is not a valid character.", exception.getMessage());
    }

    // Ein Vorzeichen ist nur ganz vorne erlaubt
    @Test
    public void positiveSignAtStartIsValidTest() {
        assertEquals("5", new BigNumber("+5", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void negativeSignAtStartWithDecimalsIsValidTest() {
        assertEquals("-5,5", new BigNumber("-5,5", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void positiveSignAtStartWithDecimalsIsValidTest() {
        assertEquals("0,5", new BigNumber("+0,5", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void negativeSignWithGroupingSeparatorIsValidTest() {
        assertEquals("-1000,5", new BigNumber("-1.000,5", Locale.GERMAN).toString(Locale.GERMAN));
    }

    @Test
    public void negativeSignInMiddleThrowsTest() {
        assertThrows(NotAValidCharacterException.class, () -> new BigNumber("1-2", Locale.GERMAN));
    }

    @Test
    public void negativeSignAtEndThrowsTest() {
        assertThrows(NotAValidCharacterException.class, () -> new BigNumber("5-", Locale.GERMAN));
    }

    @Test
    public void negativeSignAfterDecimalsThrowsTest() {
        assertThrows(NotAValidCharacterException.class, () -> new BigNumber("5,5-", Locale.GERMAN));
    }

    @Test
    public void negativeSignAfterDecimalSeparatorThrowsTest() {
        assertThrows(NotAValidCharacterException.class, () -> new BigNumber("5,-5", Locale.GERMAN));
    }

    @Test
    public void positiveSignInMiddleThrowsTest() {
        assertThrows(NotAValidCharacterException.class, () -> new BigNumber("1+2", Locale.GERMAN));
    }

    @Test
    public void positiveSignAtEndThrowsTest() {
        assertThrows(NotAValidCharacterException.class, () -> new BigNumber("5+", Locale.GERMAN));
    }

    @Test
    public void twoNegativeSignsThrowsTest() {
        assertThrows(NotAValidCharacterException.class, () -> new BigNumber("--5", Locale.GERMAN));
    }

    @Test
    public void twoPositiveSignsThrowsTest() {
        assertThrows(NotAValidCharacterException.class, () -> new BigNumber("++5", Locale.GERMAN));
    }

    @Test
    public void positiveAndNegativeSignThrowsTest() {
        assertThrows(NotAValidCharacterException.class, () -> new BigNumber("+-5", Locale.GERMAN));
    }

    @Test
    public void negativeAndPositiveSignThrowsTest() {
        assertThrows(NotAValidCharacterException.class, () -> new BigNumber("-+5", Locale.GERMAN));
    }

    @Test
    public void negativeSignAtStartAndEndThrowsTest() {
        assertThrows(NotAValidCharacterException.class, () -> new BigNumber("-5-", Locale.GERMAN));
    }

    @Test
    public void englishNegativeSignInMiddleThrowsTest() {
        assertThrows(NotAValidCharacterException.class, () -> new BigNumber("1-2", Locale.US));
    }

    @Test
    public void misplacedSignMessageNamesSignTest() {
        final NotAValidCharacterException exception = assertThrows(
                NotAValidCharacterException.class,
                () -> new BigNumber("1-2", Locale.GERMAN)
        );

        assertEquals("'-' is not a valid character.", exception.getMessage());
    }

}
