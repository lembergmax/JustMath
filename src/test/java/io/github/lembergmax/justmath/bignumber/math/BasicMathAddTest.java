package io.github.lembergmax.justmath.bignumber.math;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class BasicMathAddTest {

    @Test
    public void addBothPositiveTest() {
        assertEquals(
                "777,777",
                BasicMath.add(
                        new BigNumber("123,456", Locale.GERMAN),
                        new BigNumber("654,321", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
        assertEquals(
                "1999,998",
                BasicMath.add(
                        new BigNumber("999,999", Locale.GERMAN),
                        new BigNumber("999,999", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
        assertEquals(
                "0",
                BasicMath.add(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("0", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
        assertEquals(
                "0,00000002",
                BasicMath.add(
                        new BigNumber("0,00000001", Locale.GERMAN),
                        new BigNumber("0,00000001", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
        assertEquals(
                "100000000000",
                BasicMath.add(
                        new BigNumber("50000000000", Locale.GERMAN),
                        new BigNumber("50000000000", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
        assertEquals(
                "123456",
                BasicMath.add(
                        new BigNumber("123000", Locale.GERMAN),
                        new BigNumber("456", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
        assertEquals(
                "11111111100",
                BasicMath.add(
                        new BigNumber("9876543210", Locale.GERMAN),
                        new BigNumber("1234567890", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
        assertEquals(
                "180000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                BasicMath.add(
                        new BigNumber("90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN),
                        new BigNumber("90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void addFirstNegativeTest() {
        assertEquals(
                "530,865",
                BasicMath.add(
                        new BigNumber("-123,456", Locale.GERMAN),
                        new BigNumber("654,321", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
        assertEquals(
                "0",
                BasicMath.add(
                        new BigNumber("-999,999", Locale.GERMAN),
                        new BigNumber("999,999", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
        assertEquals(
                "0",
                BasicMath.add(
                        new BigNumber("-0", Locale.GERMAN),
                        new BigNumber("0", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
        assertEquals(
                "0",
                BasicMath.add(
                        new BigNumber("-0,00000001", Locale.GERMAN),
                        new BigNumber("0,00000001", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
        assertEquals(
                "0",
                BasicMath.add(
                        new BigNumber("-50000000000", Locale.GERMAN),
                        new BigNumber("50000000000", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
        assertEquals(
                "-122544",
                BasicMath.add(
                        new BigNumber("-123000", Locale.GERMAN),
                        new BigNumber("456", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
        assertEquals(
                "-8641975320",
                BasicMath.add(
                        new BigNumber("-9876543210", Locale.GERMAN),
                        new BigNumber("1234567890", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
        assertEquals(
                "0",
                BasicMath.add(
                        new BigNumber("-90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN),
                        new BigNumber("90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void addSecondNegativeTest() {
        assertEquals(
                "-530,865",
                BasicMath.add(
                        new BigNumber("123,456", Locale.GERMAN),
                        new BigNumber("-654,321", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
        assertEquals(
                "0",
                BasicMath.add(
                        new BigNumber("999,999", Locale.GERMAN),
                        new BigNumber("-999,999", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
        assertEquals(
                "0",
                BasicMath.add(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("-0", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
        assertEquals(
                "0",
                BasicMath.add(
                        new BigNumber("0,00000001", Locale.GERMAN),
                        new BigNumber("-0,00000001", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
        assertEquals(
                "0",
                BasicMath.add(
                        new BigNumber("50000000000", Locale.GERMAN),
                        new BigNumber("-50000000000", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
        assertEquals(
                "122544",
                BasicMath.add(
                        new BigNumber("123000", Locale.GERMAN),
                        new BigNumber("-456", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
        assertEquals(
                "8641975320",
                BasicMath.add(
                        new BigNumber("9876543210", Locale.GERMAN),
                        new BigNumber("-1234567890", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
        assertEquals(
                "0",
                BasicMath.add(
                        new BigNumber("90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN),
                        new BigNumber("-90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void addBothNegativeTest() {
        assertEquals(
                "-777,777",
                BasicMath.add(
                        new BigNumber("-123,456", Locale.GERMAN),
                        new BigNumber("-654,321", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
        assertEquals(
                "-1999,998",
                BasicMath.add(
                        new BigNumber("-999,999", Locale.GERMAN),
                        new BigNumber("-999,999", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
        assertEquals(
                "0",
                BasicMath.add(
                        new BigNumber("-0", Locale.GERMAN),
                        new BigNumber("-0", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
        assertEquals(
                "-0,00000002",
                BasicMath.add(
                        new BigNumber("-0,00000001", Locale.GERMAN),
                        new BigNumber("-0,00000001", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
        assertEquals(
                "-100000000000",
                BasicMath.add(
                        new BigNumber("-50000000000", Locale.GERMAN),
                        new BigNumber("-50000000000", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
        assertEquals(
                "-123456",
                BasicMath.add(
                        new BigNumber("-123000", Locale.GERMAN),
                        new BigNumber("-456", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
        assertEquals(
                "-11111111100",
                BasicMath.add(
                        new BigNumber("-9876543210", Locale.GERMAN),
                        new BigNumber("-1234567890", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
        assertEquals(
                "-180000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                BasicMath.add(
                        new BigNumber("-90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN),
                        new BigNumber("-90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    // Ohne Übertrag
    @Test
    public void addSingleDigitsTest() {
        assertEquals(
                "5",
                BasicMath.add(
                        new BigNumber("2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    // Übertrag
    @Test
    public void addCarryIntoNewDigitTest() {
        assertEquals(
                "10",
                BasicMath.add(
                        new BigNumber("9", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void addCarryOverMultipleDigitsTest() {
        assertEquals(
                "1000",
                BasicMath.add(
                        new BigNumber("999", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void addCarryFollowedByDigitsWithoutCarryTest() {
        assertEquals(
                "423",
                BasicMath.add(
                        new BigNumber("105", Locale.GERMAN),
                        new BigNumber("318", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void addCarryOverDecimalSeparatorTest() {
        assertEquals(
                "10,1",
                BasicMath.add(
                        new BigNumber("9,9", Locale.GERMAN),
                        new BigNumber("0,2", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void addDecimalsCarryIntoIntegerTest() {
        assertEquals(
                "1",
                BasicMath.add(
                        new BigNumber("0,5", Locale.GERMAN),
                        new BigNumber("0,5", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    // Unterschiedliche Längen
    @Test
    public void addDifferentAfterDecimalLengthsTest() {
        assertEquals(
                "1,001",
                BasicMath.add(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("0,001", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void addDifferentBeforeDecimalLengthsTest() {
        assertEquals(
                "1001,25",
                BasicMath.add(
                        new BigNumber("1000,5", Locale.GERMAN),
                        new BigNumber("0,75", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void addShorterNumberFirstTest() {
        assertEquals(
                "1001,25",
                BasicMath.add(
                        new BigNumber("0,75", Locale.GERMAN),
                        new BigNumber("1000,5", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    // Normalisierung des Ergebnisses
    @Test
    public void addResultWithoutDecimalsTest() {
        assertEquals(
                "3",
                BasicMath.add(
                        new BigNumber("1,25", Locale.GERMAN),
                        new BigNumber("1,75", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void addResultWithTrailingZeroRemovedTest() {
        assertEquals(
                "1,5",
                BasicMath.add(
                        new BigNumber("1,25", Locale.GERMAN),
                        new BigNumber("0,25", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void addResultWithoutCarryHasNoLeadingZeroTest() {
        assertEquals(
                "0,3",
                BasicMath.add(
                        new BigNumber("0,1", Locale.GERMAN),
                        new BigNumber("0,2", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    // Null als Operand
    @Test
    public void addZeroToPositiveTest() {
        assertEquals(
                "5",
                BasicMath.add(
                        new BigNumber("5", Locale.GERMAN),
                        new BigNumber("0", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void addPositiveToZeroTest() {
        assertEquals(
                "5",
                BasicMath.add(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("5", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void addZeroToNegativeTest() {
        assertEquals(
                "-5",
                BasicMath.add(
                        new BigNumber("-5", Locale.GERMAN),
                        new BigNumber("0", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void addNegativeToZeroTest() {
        assertEquals(
                "-5",
                BasicMath.add(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("-5", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    // -0 hat denselben Wert wie 0
    @Test
    public void addNegativeZeroToPositiveTest() {
        assertEquals(
                "5",
                BasicMath.add(
                        new BigNumber("5", Locale.GERMAN),
                        new BigNumber("-0", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void addPositiveToNegativeZeroTest() {
        assertEquals(
                "5",
                BasicMath.add(
                        new BigNumber("-0", Locale.GERMAN),
                        new BigNumber("5", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void addNegativeZeroToNegativeTest() {
        assertEquals(
                "-5",
                BasicMath.add(
                        new BigNumber("-5", Locale.GERMAN),
                        new BigNumber("-0", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void addNegativeToNegativeZeroTest() {
        assertEquals(
                "-5",
                BasicMath.add(
                        new BigNumber("-0", Locale.GERMAN),
                        new BigNumber("-5", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    // Gleicher Betrag mit unterschiedlichen Vorzeichen
    // + -
    @Test
    public void addPositiveWithNegativeSameAbsoluteResultIsNotNegativeTest() {
        assertFalse(
                BasicMath.add(
                        new BigNumber("5", Locale.GERMAN),
                        new BigNumber("-5", Locale.GERMAN),
                        Locale.GERMAN
                ).isNegative()
        );
    }

    // - +
    @Test
    public void addNegativeWithPositiveSameAbsoluteResultIsNotNegativeTest() {
        assertFalse(
                BasicMath.add(
                        new BigNumber("-5", Locale.GERMAN),
                        new BigNumber("5", Locale.GERMAN),
                        Locale.GERMAN
                ).isNegative()
        );
    }

    // Vorzeichen des Ergebnisses
    // + +
    @Test
    public void addWithExplicitPositiveSignsTest() {
        assertEquals(
                "8",
                BasicMath.add(
                        new BigNumber("+5", Locale.GERMAN),
                        new BigNumber("+3", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    // + -
    @Test
    public void addPositiveWithSmallerNegativeTest() {
        assertEquals(
                "2",
                BasicMath.add(
                        new BigNumber("5", Locale.GERMAN),
                        new BigNumber("-3", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    // + -
    @Test
    public void addPositiveWithLargerNegativeTest() {
        assertEquals(
                "-2",
                BasicMath.add(
                        new BigNumber("3", Locale.GERMAN),
                        new BigNumber("-5", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    // - +
    @Test
    public void addNegativeWithSmallerPositiveTest() {
        assertEquals(
                "-2",
                BasicMath.add(
                        new BigNumber("-5", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    // - +
    @Test
    public void addNegativeWithLargerPositiveTest() {
        assertEquals(
                "2",
                BasicMath.add(
                        new BigNumber("-3", Locale.GERMAN),
                        new BigNumber("5", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    // - -
    @Test
    public void addBothNegativeCarryIntoNewDigitTest() {
        assertEquals(
                "-1000",
                BasicMath.add(
                        new BigNumber("-999", Locale.GERMAN),
                        new BigNumber("-1", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    // - -
    @Test
    public void addBothNegativeDecimalsCarryIntoIntegerTest() {
        assertEquals(
                "-1",
                BasicMath.add(
                        new BigNumber("-0,5", Locale.GERMAN),
                        new BigNumber("-0,5", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void addOnlyDecimalsWithNegativeResultTest() {
        assertEquals(
                "-0,1",
                BasicMath.add(
                        new BigNumber("0,1", Locale.GERMAN),
                        new BigNumber("-0,2", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    // Unterschiedliche Vorzeichen: es wird intern subtrahiert und dabei geborgt
    // + -
    @Test
    public void addPositiveWithNegativeBorrowOverMultipleZerosTest() {
        assertEquals(
                "999",
                BasicMath.add(
                        new BigNumber("1000", Locale.GERMAN),
                        new BigNumber("-1", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    // - +
    @Test
    public void addNegativeWithPositiveBorrowOverMultipleZerosTest() {
        assertEquals(
                "-999",
                BasicMath.add(
                        new BigNumber("-1000", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    // + -
    @Test
    public void addPositiveWithLongerNegativeTest() {
        assertEquals(
                "-999",
                BasicMath.add(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("-1000", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    // - +
    @Test
    public void addNegativeWithLongerPositiveTest() {
        assertEquals(
                "999",
                BasicMath.add(
                        new BigNumber("-1", Locale.GERMAN),
                        new BigNumber("1000", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    // + -
    @Test
    public void addPositiveWithNegativeDifferentAfterDecimalLengthsTest() {
        assertEquals(
                "0,999",
                BasicMath.add(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("-0,001", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    // Sehr große und sehr kleine Zahlen
    @Test
    public void addVeryLargeValuesCarryIntoNewDigitTest() {
        assertEquals(
                "1000000000000000000000000000000",
                BasicMath.add(
                        new BigNumber("999999999999999999999999999999", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void addVeryLargeValueWithNegativeTest() {
        assertEquals(
                "999999999999999999999999999999",
                BasicMath.add(
                        new BigNumber("1000000000000000000000000000000", Locale.GERMAN),
                        new BigNumber("-1", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void addVerySmallToVeryLargeValueTest() {
        assertEquals(
                "90000000000,00000001",
                BasicMath.add(
                        new BigNumber("90000000000", Locale.GERMAN),
                        new BigNumber("0,00000001", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void addVerySmallValuesWithNegativeResultTest() {
        assertEquals(
                "-0,00000001",
                BasicMath.add(
                        new BigNumber("0,00000001", Locale.GERMAN),
                        new BigNumber("-0,00000002", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    // Locale
    @Test
    public void addWithEnglishLocaleTest() {
        assertEquals(
                "7.75",
                BasicMath.add(
                        new BigNumber("5.5", Locale.US),
                        new BigNumber("2.25", Locale.US),
                        Locale.US
                ).toString()
        );
    }

    @Test
    public void addWithEnglishLocaleAndNegativeResultTest() {
        assertEquals(
                "-3.25",
                BasicMath.add(
                        new BigNumber("-5.5", Locale.US),
                        new BigNumber("2.25", Locale.US),
                        Locale.US
                ).toString()
        );
    }

    @Test
    public void addWithGermanGroupingSeparatorTest() {
        assertEquals(
                "1001",
                BasicMath.add(
                        new BigNumber("1.000,5", Locale.GERMAN),
                        new BigNumber("0,5", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void addWithEnglishGroupingSeparatorTest() {
        assertEquals(
                "1001",
                BasicMath.add(
                        new BigNumber("1,000.5", Locale.US),
                        new BigNumber("0.5", Locale.US),
                        Locale.US
                ).toString()
        );
    }

    // Das Ergebnis behält die Locale des ersten Summanden
    @Test
    public void addWithGermanAndEnglishLocaleTest() {
        assertEquals(
                "4,75",
                BasicMath.add(
                        new BigNumber("3,5", Locale.GERMAN),
                        new BigNumber("1.25", Locale.US),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void addWithEnglishAndGermanLocaleTest() {
        assertEquals(
                "4.75",
                BasicMath.add(
                        new BigNumber("1.25", Locale.US),
                        new BigNumber("3,5", Locale.GERMAN),
                        Locale.US
                ).toString()
        );
    }

    // Der größere Summand bestimmt den String-Aufbau vor dem Gleichmachen der Längen,
    // daher muss auch sein Dezimaltrenner (nicht nur der von addend1) erkannt werden
    @Test
    public void addRecognizesDecimalSeparatorOfLargerSecondAddendWithDifferentLocaleTest() {
        assertEquals(
                "4.5",
                BasicMath.add(
                        new BigNumber("1", Locale.US),
                        new BigNumber("3,5", Locale.GERMAN),
                        Locale.US
                ).toString()
        );
    }

    @Test
    public void addWithTargetLocaleDifferentFromBothAddendsTest() {
        assertEquals(
                "3.75",
                BasicMath.add(
                        new BigNumber("2,5", Locale.GERMAN),
                        new BigNumber("1,25", Locale.GERMAN),
                        Locale.US
                ).toString()
        );
    }

    @Test
    public void addSameAbsoluteWithDifferentSignsAndLocalesTest() {
        assertEquals(
                "0",
                BasicMath.add(
                        new BigNumber("2,5", Locale.GERMAN),
                        new BigNumber("-2.5", Locale.US),
                        Locale.GERMAN
                ).toString()
        );
    }

    // Eingabewerte dürfen nicht verändert werden
    @Test
    public void addDoesNotChangeOperandsTest() {
        final BigNumber addend1 = new BigNumber("5", Locale.GERMAN);
        final BigNumber addend2 = new BigNumber("-8", Locale.GERMAN);

        BasicMath.add(addend1, addend2, addend1.getLocale());

        assertEquals("5", addend1.toString());
        assertEquals("-8", addend2.toString());
    }

}
