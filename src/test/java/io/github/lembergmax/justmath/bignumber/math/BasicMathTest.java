package io.github.lembergmax.justmath.bignumber.math;

import org.junit.jupiter.api.Test;

import io.github.lembergmax.justmath.bignumber.BigNumber;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class BasicMathTest {

    @Test
    public void addBothPositiveTest() {
        assertEquals(
                "777,777",
                BasicMath.add(
                        new BigNumber("123,456", Locale.GERMAN),
                        new BigNumber("654,321", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "1999,998",
                BasicMath.add(
                        new BigNumber("999,999", Locale.GERMAN),
                        new BigNumber("999,999", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "0",
                BasicMath.add(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("0", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "0,00000002",
                BasicMath.add(
                        new BigNumber("0,00000001", Locale.GERMAN),
                        new BigNumber("0,00000001", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "100000000000",
                BasicMath.add(
                        new BigNumber("50000000000", Locale.GERMAN),
                        new BigNumber("50000000000", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "123456",
                BasicMath.add(
                        new BigNumber("123000", Locale.GERMAN),
                        new BigNumber("456", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "11111111100",
                BasicMath.add(
                        new BigNumber("9876543210", Locale.GERMAN),
                        new BigNumber("1234567890", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "180000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                BasicMath.add(
                        new BigNumber("90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN),
                        new BigNumber("90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void addFirstNegativeTest() {
        assertEquals(
                "530,865",
                BasicMath.add(
                        new BigNumber("-123,456", Locale.GERMAN),
                        new BigNumber("654,321", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "0",
                BasicMath.add(
                        new BigNumber("-999,999", Locale.GERMAN),
                        new BigNumber("999,999", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "0",
                BasicMath.add(
                        new BigNumber("-0", Locale.GERMAN),
                        new BigNumber("0", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "0",
                BasicMath.add(
                        new BigNumber("-0,00000001", Locale.GERMAN),
                        new BigNumber("0,00000001", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "0",
                BasicMath.add(
                        new BigNumber("-50000000000", Locale.GERMAN),
                        new BigNumber("50000000000", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "-122544",
                BasicMath.add(
                        new BigNumber("-123000", Locale.GERMAN),
                        new BigNumber("456", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "-8641975320",
                BasicMath.add(
                        new BigNumber("-9876543210", Locale.GERMAN),
                        new BigNumber("1234567890", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "0",
                BasicMath.add(
                        new BigNumber("-90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN),
                        new BigNumber("90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void addSecondNegativeTest() {
        assertEquals(
                "-530,865",
                BasicMath.add(
                        new BigNumber("123,456", Locale.GERMAN),
                        new BigNumber("-654,321", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "0",
                BasicMath.add(
                        new BigNumber("999,999", Locale.GERMAN),
                        new BigNumber("-999,999", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "0",
                BasicMath.add(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("-0", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "0",
                BasicMath.add(
                        new BigNumber("0,00000001", Locale.GERMAN),
                        new BigNumber("-0,00000001", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "0",
                BasicMath.add(
                        new BigNumber("50000000000", Locale.GERMAN),
                        new BigNumber("-50000000000", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "122544",
                BasicMath.add(
                        new BigNumber("123000", Locale.GERMAN),
                        new BigNumber("-456", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "8641975320",
                BasicMath.add(
                        new BigNumber("9876543210", Locale.GERMAN),
                        new BigNumber("-1234567890", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "0",
                BasicMath.add(
                        new BigNumber("90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN),
                        new BigNumber("-90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void addBothNegativeTest() {
        assertEquals(
                "-777,777",
                BasicMath.add(
                        new BigNumber("-123,456", Locale.GERMAN),
                        new BigNumber("-654,321", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "-1999,998",
                BasicMath.add(
                        new BigNumber("-999,999", Locale.GERMAN),
                        new BigNumber("-999,999", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "0",
                BasicMath.add(
                        new BigNumber("-0", Locale.GERMAN),
                        new BigNumber("-0", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "-0,00000002",
                BasicMath.add(
                        new BigNumber("-0,00000001", Locale.GERMAN),
                        new BigNumber("-0,00000001", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "-100000000000",
                BasicMath.add(
                        new BigNumber("-50000000000", Locale.GERMAN),
                        new BigNumber("-50000000000", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "-123456",
                BasicMath.add(
                        new BigNumber("-123000", Locale.GERMAN),
                        new BigNumber("-456", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "-11111111100",
                BasicMath.add(
                        new BigNumber("-9876543210", Locale.GERMAN),
                        new BigNumber("-1234567890", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "-180000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                BasicMath.add(
                        new BigNumber("-90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN),
                        new BigNumber("-90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN)
                ).toString()
        );
    }

    // + +
    @Test
    public void subtractPositiveValuesTest() {
        assertEquals(
                "988,96",
                BasicMath.subtract(
                        new BigNumber("2512,78", Locale.GERMAN),
                        new BigNumber("1523,82", Locale.GERMAN)
                ).toString()
        );
    }

    // - -
    @Test
    public void subtractNegativeValuesTest() {
        assertEquals(
                "-988,96",
                BasicMath.subtract(
                        new BigNumber("-2512,78", Locale.GERMAN),
                        new BigNumber("-1523,82", Locale.GERMAN)
                ).toString()
        );
    }

    // - +
    @Test
    public void subtractNegativeWithPositiveTest() {
        assertEquals(
                "-4036,6",
                BasicMath.subtract(
                        new BigNumber("-2512,78", Locale.GERMAN),
                        new BigNumber("+1523,82", Locale.GERMAN)
                ).toString()
        );
    }

    // + -
    @Test
    public void subtractPositiveWithNegativeTest() {
        assertEquals(
                "4036,6",
                BasicMath.subtract(
                        new BigNumber("2512,78", Locale.GERMAN),
                        new BigNumber("-1523,82", Locale.GERMAN)
                ).toString()
        );
    }

    // + +
    @Test
    public void subtractPositiveValuesAndMinuendSmallerThanSubtrahendTest() {
        assertEquals(
                "-988,96",
                BasicMath.subtract(
                        new BigNumber("1523,82", Locale.GERMAN),
                        new BigNumber("2512,78", Locale.GERMAN)
                ).toString()
        );
    }

    // - -
    @Test
    public void subtractNegativeValuesAndMinuendSmallerThanSubtrahendTest() {
        assertEquals(
                "988,96",
                BasicMath.subtract(
                        new BigNumber("-1523,82", Locale.GERMAN),
                        new BigNumber("-2512,78", Locale.GERMAN)
                ).toString()
        );
    }

    // - +
    @Test
    public void subtractNegativeWithPositiveAndMinuendSmallerThanSubtrahendTest() {
        assertEquals(
                "-4036,6",
                BasicMath.subtract(
                        new BigNumber("-1523,82", Locale.GERMAN),
                        new BigNumber("+2512,78", Locale.GERMAN)
                ).toString()
        );
    }

    // + -
    @Test
    public void subtractPositiveWithNegativeAndMinuendSmallerThanSubtrahendTest() {
        assertEquals(
                "4036,6",
                BasicMath.subtract(
                        new BigNumber("1523,82", Locale.GERMAN),
                        new BigNumber("-2512,78", Locale.GERMAN)
                ).toString()
        );
    }

    // Ohne Übertrag: die erste Ziffer darf nicht verloren gehen
    @Test
    public void subtractSingleDigitsTest() {
        assertEquals(
                "2",
                BasicMath.subtract(
                        new BigNumber("5", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void subtractWithoutBorrowTest() {
        assertEquals(
                "864,2",
                BasicMath.subtract(
                        new BigNumber("987,65", Locale.GERMAN),
                        new BigNumber("123,45", Locale.GERMAN)
                ).toString()
        );
    }

    // Übertrag
    @Test
    public void subtractBorrowOverMultipleZerosTest() {
        assertEquals(
                "999",
                BasicMath.subtract(
                        new BigNumber("1000", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void subtractBorrowOnConsecutiveDigitsTest() {
        assertEquals(
                "3445",
                BasicMath.subtract(
                        new BigNumber("5432", Locale.GERMAN),
                        new BigNumber("1987", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void subtractBorrowFollowedByDigitsWithoutBorrowTest() {
        assertEquals(
                "418",
                BasicMath.subtract(
                        new BigNumber("523", Locale.GERMAN),
                        new BigNumber("105", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void subtractBorrowOverDecimalSeparatorTest() {
        assertEquals(
                "9,9",
                BasicMath.subtract(
                        new BigNumber("10,1", Locale.GERMAN),
                        new BigNumber("0,2", Locale.GERMAN)
                ).toString()
        );
    }

    // Unterschiedliche Längen
    @Test
    public void subtractDifferentAfterDecimalLengthsTest() {
        assertEquals(
                "0,999",
                BasicMath.subtract(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("0,001", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void subtractDifferentBeforeDecimalLengthsTest() {
        assertEquals(
                "999,75",
                BasicMath.subtract(
                        new BigNumber("1000,5", Locale.GERMAN),
                        new BigNumber("0,75", Locale.GERMAN)
                ).toString()
        );
    }

    // Normalisierung des Ergebnisses
    @Test
    public void subtractResultWithoutDecimalsTest() {
        assertEquals(
                "1",
                BasicMath.subtract(
                        new BigNumber("1,5", Locale.GERMAN),
                        new BigNumber("0,5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void subtractResultWithLeadingZerosRemovedTest() {
        assertEquals(
                "1",
                BasicMath.subtract(
                        new BigNumber("1000,5", Locale.GERMAN),
                        new BigNumber("999,5", Locale.GERMAN)
                ).toString()
        );
    }

    // Null als Operand
    @Test
    public void subtractZeroFromPositiveTest() {
        assertEquals(
                "5",
                BasicMath.subtract(
                        new BigNumber("5", Locale.GERMAN),
                        new BigNumber("0", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void subtractZeroFromNegativeTest() {
        assertEquals(
                "-5",
                BasicMath.subtract(
                        new BigNumber("-5", Locale.GERMAN),
                        new BigNumber("0", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void subtractPositiveFromZeroTest() {
        assertEquals(
                "-5",
                BasicMath.subtract(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void subtractNegativeFromZeroTest() {
        assertEquals(
                "5",
                BasicMath.subtract(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("-5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void subtractZeroFromZeroTest() {
        assertEquals(
                "0",
                BasicMath.subtract(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("0", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void subtractNegativeZeroFromNegativeTest() {
        assertEquals(
                "-5",
                BasicMath.subtract(
                        new BigNumber("-5", Locale.GERMAN),
                        new BigNumber("-0", Locale.GERMAN)
                ).toString()
        );
    }

    // Gleiche Werte
    @Test
    public void subtractEqualPositiveValuesTest() {
        assertEquals(
                "0",
                BasicMath.subtract(
                        new BigNumber("123,456", Locale.GERMAN),
                        new BigNumber("123,456", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void subtractEqualNegativeValuesTest() {
        assertEquals(
                "0",
                BasicMath.subtract(
                        new BigNumber("-123,456", Locale.GERMAN),
                        new BigNumber("-123,456", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void subtractEqualValuesResultIsNotNegativeTest() {
        assertFalse(
                BasicMath.subtract(
                        new BigNumber("5", Locale.GERMAN),
                        new BigNumber("5", Locale.GERMAN)
                ).isNegative()
        );
    }

    // Vorzeichen des Ergebnisses
    // + +
    @Test
    public void subtractPositiveValuesWithNegativeResultTest() {
        assertEquals(
                "-3",
                BasicMath.subtract(
                        new BigNumber("5", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN)
                ).toString()
        );
    }

    // - -
    @Test
    public void subtractNegativeValuesWithPositiveResultTest() {
        assertEquals(
                "3",
                BasicMath.subtract(
                        new BigNumber("-5", Locale.GERMAN),
                        new BigNumber("-8", Locale.GERMAN)
                ).toString()
        );
    }

    // - -
    @Test
    public void subtractNegativeValuesWithNegativeResultTest() {
        assertEquals(
                "-3",
                BasicMath.subtract(
                        new BigNumber("-8", Locale.GERMAN),
                        new BigNumber("-5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void subtractOnlyDecimalsWithNegativeResultTest() {
        assertEquals(
                "-0,1",
                BasicMath.subtract(
                        new BigNumber("0,1", Locale.GERMAN),
                        new BigNumber("0,2", Locale.GERMAN)
                ).toString()
        );
    }

    // Unterschiedliche Vorzeichen: der letzte Übertrag darf nicht verloren gehen
    // + -
    @Test
    public void subtractPositiveWithNegativeNewDigitTest() {
        assertEquals(
                "1000",
                BasicMath.subtract(
                        new BigNumber("999", Locale.GERMAN),
                        new BigNumber("-1", Locale.GERMAN)
                ).toString()
        );
    }

    // - +
    @Test
    public void subtractNegativeWithPositiveNewDigitTest() {
        assertEquals(
                "-1000",
                BasicMath.subtract(
                        new BigNumber("-999", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN)
                ).toString()
        );
    }

    // + -
    @Test
    public void subtractPositiveWithNegativeDecimalsCarryIntoIntegerTest() {
        assertEquals(
                "1",
                BasicMath.subtract(
                        new BigNumber("0,5", Locale.GERMAN),
                        new BigNumber("-0,5", Locale.GERMAN)
                ).toString()
        );
    }

    // Sehr große und sehr kleine Zahlen
    @Test
    public void subtractVeryLargeValuesTest() {
        assertEquals(
                "999999999999999999999999999999",
                BasicMath.subtract(
                        new BigNumber("1000000000000000000000000000000", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void subtractVerySmallValuesTest() {
        assertEquals(
                "0,00000001",
                BasicMath.subtract(
                        new BigNumber("0,00000002", Locale.GERMAN),
                        new BigNumber("0,00000001", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void subtractVerySmallFromVeryLargeValueTest() {
        assertEquals(
                "89999999999,99999999",
                BasicMath.subtract(
                        new BigNumber("90000000000", Locale.GERMAN),
                        new BigNumber("0,00000001", Locale.GERMAN)
                ).toString()
        );
    }

    // Locale
    @Test
    public void subtractWithEnglishLocaleTest() {
        assertEquals(
                "3.25",
                BasicMath.subtract(
                        new BigNumber("5.5", Locale.US),
                        new BigNumber("2.25", Locale.US)
                ).toString()
        );
    }

    @Test
    public void subtractWithEnglishLocaleAndNegativeResultTest() {
        assertEquals(
                "-2.75",
                BasicMath.subtract(
                        new BigNumber("5.5", Locale.US),
                        new BigNumber("8.25", Locale.US)
                ).toString()
        );
    }

    @Test
    public void subtractWithGermanGroupingSeparatorTest() {
        assertEquals(
                "1000",
                BasicMath.subtract(
                        new BigNumber("1.000,5", Locale.GERMAN),
                        new BigNumber("0,5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void subtractWithEnglishGroupingSeparatorTest() {
        assertEquals(
                "1000",
                BasicMath.subtract(
                        new BigNumber("1,000.5", Locale.US),
                        new BigNumber("0.5", Locale.US)
                ).toString()
        );
    }

    // Das Ergebnis behält die Locale des ursprünglichen Minuenden
    @Test
    public void subtractWithDifferentLocalesTest() {
        assertEquals(
                "2,25",
                BasicMath.subtract(
                        new BigNumber("3,5", Locale.GERMAN),
                        new BigNumber("1.25", Locale.US)
                ).toString()
        );
    }

    @Test
    public void subtractWithDifferentLocalesAndMinuendSmallerThanSubtrahendTest() {
        assertEquals(
                "-2,25",
                BasicMath.subtract(
                        new BigNumber("1,25", Locale.GERMAN),
                        new BigNumber("3.5", Locale.US)
                ).toString()
        );
    }

    @Test
    public void subtractEqualValuesWithDifferentLocalesTest() {
        assertEquals(
                "0",
                BasicMath.subtract(
                        new BigNumber("2,5", Locale.GERMAN),
                        new BigNumber("2.5", Locale.US)
                ).toString()
        );
    }

    // Eingabewerte dürfen nicht verändert werden
    @Test
    public void subtractDoesNotChangeOperandsTest() {
        final BigNumber minuend = new BigNumber("5", Locale.GERMAN);
        final BigNumber subtrahend = new BigNumber("8", Locale.GERMAN);

        BasicMath.subtract(minuend, subtrahend);

        assertEquals("5", minuend.toString());
        assertEquals("8", subtrahend.toString());
    }
}
