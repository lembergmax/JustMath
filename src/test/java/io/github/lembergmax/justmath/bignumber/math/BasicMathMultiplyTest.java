package io.github.lembergmax.justmath.bignumber.math;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BasicMathMultiplyTest {

    @Test
    public void multiplyBothPositiveTest() {
        assertEquals(
                "60",
                BasicMath.multiply(
                        new BigNumber("12", Locale.GERMAN),
                        new BigNumber("5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyByZeroTest() {
        assertEquals(
                "0",
                BasicMath.multiply(
                        new BigNumber("12345", Locale.GERMAN),
                        new BigNumber("0", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyZeroByZeroTest() {
        assertEquals(
                "0",
                BasicMath.multiply(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("0", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyFirstNegativeTest() {
        assertEquals(
                "-60",
                BasicMath.multiply(
                        new BigNumber("-12", Locale.GERMAN),
                        new BigNumber("5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplySecondNegativeTest() {
        assertEquals(
                "-60",
                BasicMath.multiply(
                        new BigNumber("12", Locale.GERMAN),
                        new BigNumber("-5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyBothNegativeTest() {
        assertEquals(
                "60",
                BasicMath.multiply(
                        new BigNumber("-12", Locale.GERMAN),
                        new BigNumber("-5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyWithDecimalsTest() {
        assertEquals(
                "2,5",
                BasicMath.multiply(
                        new BigNumber("1,25", Locale.GERMAN),
                        new BigNumber("2", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyDecimalsWithTrailingZeroTest() {
        assertEquals(
                "0,05",
                BasicMath.multiply(
                        new BigNumber("0,1", Locale.GERMAN),
                        new BigNumber("0,5", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyCarryOverMultipleDigitsTest() {
        assertEquals(
                "9876401235",
                BasicMath.multiply(
                        new BigNumber("99999", Locale.GERMAN),
                        new BigNumber("98765", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyByOneTest() {
        assertEquals(
                "12345",
                BasicMath.multiply(
                        new BigNumber("12345", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyResultIsNotNegativeWhenBothNegativeTest() {
        assertFalse(
                BasicMath.multiply(
                        new BigNumber("-3", Locale.GERMAN),
                        new BigNumber("-4", Locale.GERMAN)
                ).isNegative()
        );
    }

    @Test
    public void multiplyWithEnglishLocaleTest() {
        assertEquals(
                "7.5",
                BasicMath.multiply(
                        new BigNumber("2.5", Locale.US),
                        new BigNumber("3", Locale.US)
                ).toString()
        );
    }

    // Vorzeichen-Matrix wie in BasicMathAddTest
    @Test
    public void multiplyBothPositiveSixDigitDecimalsTest() {
        assertEquals(
                "80779,853376",
                BasicMath.multiply(
                        new BigNumber("123,456", Locale.GERMAN),
                        new BigNumber("654,321", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyBothPositiveRepeatedNinesTest() {
        assertEquals(
                "999998,000001",
                BasicMath.multiply(
                        new BigNumber("999,999", Locale.GERMAN),
                        new BigNumber("999,999", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyBothPositiveZeroFactorTest() {
        assertEquals(
                "0",
                BasicMath.multiply(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("123,456", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyBothPositiveVerySmallDecimalsTest() {
        assertEquals(
                "0,0000000000000002",
                BasicMath.multiply(
                        new BigNumber("0,00000001", Locale.GERMAN),
                        new BigNumber("0,00000002", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyBothPositiveElevenDigitIntegersTest() {
        assertEquals(
                "1250000000000000000000",
                BasicMath.multiply(
                        new BigNumber("50000000000", Locale.GERMAN),
                        new BigNumber("25000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyBothPositiveDifferentLengthsTest() {
        assertEquals(
                "56088000",
                BasicMath.multiply(
                        new BigNumber("123000", Locale.GERMAN),
                        new BigNumber("456", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyBothPositiveTenDigitIntegersTest() {
        assertEquals(
                "12193263111263526900",
                BasicMath.multiply(
                        new BigNumber("9876543210", Locale.GERMAN),
                        new BigNumber("1234567890", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyBothPositiveVeryLargeIntegersTest() {
        assertEquals(
                "2700000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                BasicMath.multiply(
                        new BigNumber("90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN),
                        new BigNumber("30000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyFirstNegativeSixDigitDecimalsTest() {
        assertEquals(
                "-80779,853376",
                BasicMath.multiply(
                        new BigNumber("-123,456", Locale.GERMAN),
                        new BigNumber("654,321", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyFirstNegativeRepeatedNinesTest() {
        assertEquals(
                "-999998,000001",
                BasicMath.multiply(
                        new BigNumber("-999,999", Locale.GERMAN),
                        new BigNumber("999,999", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyFirstNegativeZeroFactorTest() {
        assertEquals(
                "0",
                BasicMath.multiply(
                        new BigNumber("-0", Locale.GERMAN),
                        new BigNumber("123,456", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyFirstNegativeVerySmallDecimalsTest() {
        assertEquals(
                "-0,0000000000000002",
                BasicMath.multiply(
                        new BigNumber("-0,00000001", Locale.GERMAN),
                        new BigNumber("0,00000002", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyFirstNegativeElevenDigitIntegersTest() {
        assertEquals(
                "-1250000000000000000000",
                BasicMath.multiply(
                        new BigNumber("-50000000000", Locale.GERMAN),
                        new BigNumber("25000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyFirstNegativeDifferentLengthsTest() {
        assertEquals(
                "-56088000",
                BasicMath.multiply(
                        new BigNumber("-123000", Locale.GERMAN),
                        new BigNumber("456", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyFirstNegativeTenDigitIntegersTest() {
        assertEquals(
                "-12193263111263526900",
                BasicMath.multiply(
                        new BigNumber("-9876543210", Locale.GERMAN),
                        new BigNumber("1234567890", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyFirstNegativeVeryLargeIntegersTest() {
        assertEquals(
                "-2700000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                BasicMath.multiply(
                        new BigNumber("-90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN),
                        new BigNumber("30000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplySecondNegativeSixDigitDecimalsTest() {
        assertEquals(
                "-80779,853376",
                BasicMath.multiply(
                        new BigNumber("123,456", Locale.GERMAN),
                        new BigNumber("-654,321", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplySecondNegativeRepeatedNinesTest() {
        assertEquals(
                "-999998,000001",
                BasicMath.multiply(
                        new BigNumber("999,999", Locale.GERMAN),
                        new BigNumber("-999,999", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplySecondNegativeZeroFactorTest() {
        assertEquals(
                "0",
                BasicMath.multiply(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("-123,456", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplySecondNegativeVerySmallDecimalsTest() {
        assertEquals(
                "-0,0000000000000002",
                BasicMath.multiply(
                        new BigNumber("0,00000001", Locale.GERMAN),
                        new BigNumber("-0,00000002", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplySecondNegativeElevenDigitIntegersTest() {
        assertEquals(
                "-1250000000000000000000",
                BasicMath.multiply(
                        new BigNumber("50000000000", Locale.GERMAN),
                        new BigNumber("-25000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplySecondNegativeDifferentLengthsTest() {
        assertEquals(
                "-56088000",
                BasicMath.multiply(
                        new BigNumber("123000", Locale.GERMAN),
                        new BigNumber("-456", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplySecondNegativeTenDigitIntegersTest() {
        assertEquals(
                "-12193263111263526900",
                BasicMath.multiply(
                        new BigNumber("9876543210", Locale.GERMAN),
                        new BigNumber("-1234567890", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplySecondNegativeVeryLargeIntegersTest() {
        assertEquals(
                "-2700000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                BasicMath.multiply(
                        new BigNumber("90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN),
                        new BigNumber("-30000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyBothNegativeSixDigitDecimalsTest() {
        assertEquals(
                "80779,853376",
                BasicMath.multiply(
                        new BigNumber("-123,456", Locale.GERMAN),
                        new BigNumber("-654,321", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyBothNegativeRepeatedNinesTest() {
        assertEquals(
                "999998,000001",
                BasicMath.multiply(
                        new BigNumber("-999,999", Locale.GERMAN),
                        new BigNumber("-999,999", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyBothNegativeZeroFactorTest() {
        assertEquals(
                "0",
                BasicMath.multiply(
                        new BigNumber("-0", Locale.GERMAN),
                        new BigNumber("-123,456", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyBothNegativeVerySmallDecimalsTest() {
        assertEquals(
                "0,0000000000000002",
                BasicMath.multiply(
                        new BigNumber("-0,00000001", Locale.GERMAN),
                        new BigNumber("-0,00000002", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyBothNegativeElevenDigitIntegersTest() {
        assertEquals(
                "1250000000000000000000",
                BasicMath.multiply(
                        new BigNumber("-50000000000", Locale.GERMAN),
                        new BigNumber("-25000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyBothNegativeDifferentLengthsTest() {
        assertEquals(
                "56088000",
                BasicMath.multiply(
                        new BigNumber("-123000", Locale.GERMAN),
                        new BigNumber("-456", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyBothNegativeTenDigitIntegersTest() {
        assertEquals(
                "12193263111263526900",
                BasicMath.multiply(
                        new BigNumber("-9876543210", Locale.GERMAN),
                        new BigNumber("-1234567890", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyBothNegativeVeryLargeIntegersTest() {
        assertEquals(
                "2700000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                BasicMath.multiply(
                        new BigNumber("-90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN),
                        new BigNumber("-30000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN)
                ).toString()
        );
    }

    // Einstellige Faktoren und Übertrag
    @Test
    public void multiplySingleDigitsTest() {
        assertEquals(
                "12",
                BasicMath.multiply(
                        new BigNumber("3", Locale.GERMAN),
                        new BigNumber("4", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyLargestSingleDigitsTest() {
        assertEquals(
                "81",
                BasicMath.multiply(
                        new BigNumber("9", Locale.GERMAN),
                        new BigNumber("9", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyCarryIntoNewDigitTest() {
        assertEquals(
                "10",
                BasicMath.multiply(
                        new BigNumber("5", Locale.GERMAN),
                        new BigNumber("2", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyWithoutCarryTest() {
        assertEquals(
                "2222",
                BasicMath.multiply(
                        new BigNumber("1111", Locale.GERMAN),
                        new BigNumber("2", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyCarryOnEveryDigitTest() {
        assertEquals(
                "8991",
                BasicMath.multiply(
                        new BigNumber("999", Locale.GERMAN),
                        new BigNumber("9", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyCarryOverMultipleRowsTest() {
        assertEquals(
                "9801",
                BasicMath.multiply(
                        new BigNumber("99", Locale.GERMAN),
                        new BigNumber("99", Locale.GERMAN)
                ).toString()
        );
    }

    // Mehrstellige Faktoren
    @Test
    public void multiplyTwoDigitFactorsTest() {
        assertEquals(
                "408",
                BasicMath.multiply(
                        new BigNumber("12", Locale.GERMAN),
                        new BigNumber("34", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyThreeDigitFactorsTest() {
        assertEquals(
                "56088",
                BasicMath.multiply(
                        new BigNumber("123", Locale.GERMAN),
                        new BigNumber("456", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyLongFactorByShortFactorTest() {
        assertEquals(
                "370370367",
                BasicMath.multiply(
                        new BigNumber("123456789", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyShortFactorByLongFactorTest() {
        assertEquals(
                "370370367",
                BasicMath.multiply(
                        new BigNumber("3", Locale.GERMAN),
                        new BigNumber("123456789", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyZerosInsideBothFactorsTest() {
        assertEquals(
                "1002001",
                BasicMath.multiply(
                        new BigNumber("1001", Locale.GERMAN),
                        new BigNumber("1001", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyZeroInsideSecondFactorTest() {
        assertEquals(
                "12423",
                BasicMath.multiply(
                        new BigNumber("123", Locale.GERMAN),
                        new BigNumber("101", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyTrailingZerosInBothFactorsTest() {
        assertEquals(
                "360000",
                BasicMath.multiply(
                        new BigNumber("1200", Locale.GERMAN),
                        new BigNumber("300", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyPowersOfTenTest() {
        assertEquals(
                "1000000",
                BasicMath.multiply(
                        new BigNumber("1000", Locale.GERMAN),
                        new BigNumber("1000", Locale.GERMAN)
                ).toString()
        );
    }

    // Multiplikation mit Zehnerpotenzen verschiebt nur das Komma
    @Test
    public void multiplyDecimalByTenTest() {
        assertEquals(
                "12,345",
                BasicMath.multiply(
                        new BigNumber("1,2345", Locale.GERMAN),
                        new BigNumber("10", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyDecimalByThousandTest() {
        assertEquals(
                "1234,5",
                BasicMath.multiply(
                        new BigNumber("1,2345", Locale.GERMAN),
                        new BigNumber("1000", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplySmallDecimalByThousandTest() {
        assertEquals(
                "1",
                BasicMath.multiply(
                        new BigNumber("0,001", Locale.GERMAN),
                        new BigNumber("1000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyIntegerByOneTenthTest() {
        assertEquals(
                "1,2",
                BasicMath.multiply(
                        new BigNumber("12", Locale.GERMAN),
                        new BigNumber("0,1", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyIntegerByOneThousandthTest() {
        assertEquals(
                "0,012",
                BasicMath.multiply(
                        new BigNumber("12", Locale.GERMAN),
                        new BigNumber("0,001", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    // Dezimalzahlen
    @Test
    public void multiplyBothDecimalsTest() {
        assertEquals(
                "3,75",
                BasicMath.multiply(
                        new BigNumber("1,5", Locale.GERMAN),
                        new BigNumber("2,5", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyDecimalByIntegerTest() {
        assertEquals(
                "1",
                BasicMath.multiply(
                        new BigNumber("0,25", Locale.GERMAN),
                        new BigNumber("4", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyIntegerByDecimalTest() {
        assertEquals(
                "1",
                BasicMath.multiply(
                        new BigNumber("4", Locale.GERMAN),
                        new BigNumber("0,25", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyBothDecimalsWithIntegerResultTest() {
        assertEquals(
                "1",
                BasicMath.multiply(
                        new BigNumber("2,5", Locale.GERMAN),
                        new BigNumber("0,4", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyDifferentAfterDecimalLengthsTest() {
        assertEquals(
                "0,625",
                BasicMath.multiply(
                        new BigNumber("1,25", Locale.GERMAN),
                        new BigNumber("0,5", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyDecimalsCarryIntoIntegerTest() {
        assertEquals(
                "98,01",
                BasicMath.multiply(
                        new BigNumber("9,9", Locale.GERMAN),
                        new BigNumber("9,9", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyDecimalsBelowOneTest() {
        assertEquals(
                "0,25",
                BasicMath.multiply(
                        new BigNumber("0,5", Locale.GERMAN),
                        new BigNumber("0,5", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyResultNeedsLeadingZerosAfterDecimalSeparatorTest() {
        assertEquals(
                "0,000001",
                BasicMath.multiply(
                        new BigNumber("0,001", Locale.GERMAN),
                        new BigNumber("0,001", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyManyDecimalPlacesTest() {
        assertEquals(
                "0,121932631112635269",
                BasicMath.multiply(
                        new BigNumber("0,123456789", Locale.GERMAN),
                        new BigNumber("0,987654321", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyDecimalByPowerOfTenWithIntegerResultTest() {
        assertEquals(
                "5",
                BasicMath.multiply(
                        new BigNumber("0,05", Locale.GERMAN),
                        new BigNumber("100", Locale.GERMAN)
                ).toString()
        );
    }

    // Null als Faktor
    @Test
    public void multiplyZeroByPositiveTest() {
        assertEquals(
                "0",
                BasicMath.multiply(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyZeroByNegativeTest() {
        assertEquals(
                "0",
                BasicMath.multiply(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("-5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyNegativeByZeroTest() {
        assertEquals(
                "0",
                BasicMath.multiply(
                        new BigNumber("-5", Locale.GERMAN),
                        new BigNumber("0", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyNegativeZeroByPositiveTest() {
        assertEquals(
                "0",
                BasicMath.multiply(
                        new BigNumber("-0", Locale.GERMAN),
                        new BigNumber("5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyPositiveByNegativeZeroTest() {
        assertEquals(
                "0",
                BasicMath.multiply(
                        new BigNumber("5", Locale.GERMAN),
                        new BigNumber("-0", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyZeroWithDecimalPlacesByPositiveTest() {
        assertEquals(
                "0",
                BasicMath.multiply(
                        new BigNumber("0,000", Locale.GERMAN),
                        new BigNumber("5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyDecimalByZeroTest() {
        assertEquals(
                "0",
                BasicMath.multiply(
                        new BigNumber("0,5", Locale.GERMAN),
                        new BigNumber("0", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyZeroByVeryLargeIntegerTest() {
        assertEquals(
                "0",
                BasicMath.multiply(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("30000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyZeroByVerySmallDecimalTest() {
        assertEquals(
                "0",
                BasicMath.multiply(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("0,00000001", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyNegativeByZeroResultIsNotNegativeTest() {
        assertFalse(
                BasicMath.multiply(
                        new BigNumber("-5", Locale.GERMAN),
                        new BigNumber("0", Locale.GERMAN)
                ).isNegative()
        );
    }

    @Test
    public void multiplyZeroByNegativeResultIsNotNegativeTest() {
        assertFalse(
                BasicMath.multiply(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("-5", Locale.GERMAN)
                ).isNegative()
        );
    }

    @Test
    public void multiplyNegativeDecimalByZeroResultIsNotNegativeTest() {
        assertFalse(
                BasicMath.multiply(
                        new BigNumber("-0,5", Locale.GERMAN),
                        new BigNumber("0", Locale.GERMAN)
                ).isNegative()
        );
    }

    // Multiplikation mit 1 und -1
    @Test
    public void multiplyOneByIntegerTest() {
        assertEquals(
                "12345",
                BasicMath.multiply(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("12345", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyNegativeIntegerByOneTest() {
        assertEquals(
                "-12345",
                BasicMath.multiply(
                        new BigNumber("-12345", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyIntegerByNegativeOneTest() {
        assertEquals(
                "-12345",
                BasicMath.multiply(
                        new BigNumber("12345", Locale.GERMAN),
                        new BigNumber("-1", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyNegativeIntegerByNegativeOneTest() {
        assertEquals(
                "12345",
                BasicMath.multiply(
                        new BigNumber("-12345", Locale.GERMAN),
                        new BigNumber("-1", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyDecimalByOneTest() {
        assertEquals(
                "2,5",
                BasicMath.multiply(
                        new BigNumber("2,5", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyDecimalByNegativeOneTest() {
        assertEquals(
                "-2,5",
                BasicMath.multiply(
                        new BigNumber("2,5", Locale.GERMAN),
                        new BigNumber("-1", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyByOneWithDecimalPlaceTest() {
        assertEquals(
                "12345",
                BasicMath.multiply(
                        new BigNumber("12345", Locale.GERMAN),
                        new BigNumber("1,0", Locale.GERMAN)
                ).toString()
        );
    }

    // Die Reihenfolge der Faktoren spielt keine Rolle
    @Test
    public void multiplyIsCommutativeForIntegersTest() {
        final BigNumber factor1 = new BigNumber("12345", Locale.GERMAN);
        final BigNumber factor2 = new BigNumber("678", Locale.GERMAN);

        assertEquals(
                BasicMath.multiply(factor1, factor2).toString(),
                BasicMath.multiply(factor2, factor1).toString()
        );
    }

    @Test
    public void multiplyIsCommutativeForDecimalsTest() {
        final BigNumber factor1 = new BigNumber("1,25", Locale.GERMAN);
        final BigNumber factor2 = new BigNumber("0,8", Locale.GERMAN);

        assertEquals(
                BasicMath.multiply(factor1, factor2).toString(Locale.GERMAN),
                BasicMath.multiply(factor2, factor1).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyIsCommutativeForNegativeFactorsTest() {
        final BigNumber factor1 = new BigNumber("-2,5", Locale.GERMAN);
        final BigNumber factor2 = new BigNumber("4", Locale.GERMAN);

        assertEquals(
                BasicMath.multiply(factor1, factor2).toString(Locale.GERMAN),
                BasicMath.multiply(factor2, factor1).toString(Locale.GERMAN)
        );
    }

    // Vorzeichen des Ergebnisses
    @Test
    public void multiplyBothPositiveResultIsNotNegativeTest() {
        assertFalse(
                BasicMath.multiply(
                        new BigNumber("3", Locale.GERMAN),
                        new BigNumber("4", Locale.GERMAN)
                ).isNegative()
        );
    }

    @Test
    public void multiplyFirstNegativeResultIsNegativeTest() {
        assertTrue(
                BasicMath.multiply(
                        new BigNumber("-3", Locale.GERMAN),
                        new BigNumber("4", Locale.GERMAN)
                ).isNegative()
        );
    }

    @Test
    public void multiplySecondNegativeResultIsNegativeTest() {
        assertTrue(
                BasicMath.multiply(
                        new BigNumber("3", Locale.GERMAN),
                        new BigNumber("-4", Locale.GERMAN)
                ).isNegative()
        );
    }

    @Test
    public void multiplyNegativeDecimalResultIsNegativeTest() {
        assertTrue(
                BasicMath.multiply(
                        new BigNumber("-0,5", Locale.GERMAN),
                        new BigNumber("0,5", Locale.GERMAN)
                ).isNegative()
        );
    }

    @Test
    public void multiplyNegativeDecimalByPositiveDecimalTest() {
        assertEquals(
                "-3,75",
                BasicMath.multiply(
                        new BigNumber("-1,5", Locale.GERMAN),
                        new BigNumber("2,5", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyPositiveDecimalByNegativeDecimalTest() {
        assertEquals(
                "-3,75",
                BasicMath.multiply(
                        new BigNumber("1,5", Locale.GERMAN),
                        new BigNumber("-2,5", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyNegativeDecimalByNegativeDecimalTest() {
        assertEquals(
                "3,75",
                BasicMath.multiply(
                        new BigNumber("-1,5", Locale.GERMAN),
                        new BigNumber("-2,5", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyWithExplicitPositiveSignsTest() {
        assertEquals(
                "12",
                BasicMath.multiply(
                        new BigNumber("+3", Locale.GERMAN),
                        new BigNumber("+4", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyWithExplicitPositiveSignAndNegativeFactorTest() {
        assertEquals(
                "-12",
                BasicMath.multiply(
                        new BigNumber("+3", Locale.GERMAN),
                        new BigNumber("-4", Locale.GERMAN)
                ).toString()
        );
    }

    // Normalisierung des Ergebnisses
    @Test
    public void multiplyResultWithoutDecimalsTest() {
        assertEquals(
                "5",
                BasicMath.multiply(
                        new BigNumber("2,5", Locale.GERMAN),
                        new BigNumber("2", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyResultWithTrailingZerosRemovedTest() {
        assertEquals(
                "0,1",
                BasicMath.multiply(
                        new BigNumber("0,25", Locale.GERMAN),
                        new BigNumber("0,4", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyResultWithoutLeadingZerosTest() {
        assertEquals(
                "10",
                BasicMath.multiply(
                        new BigNumber("0,5", Locale.GERMAN),
                        new BigNumber("20", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyResultBelowOneHasOneLeadingZeroTest() {
        assertEquals(
                "0,06",
                BasicMath.multiply(
                        new BigNumber("0,2", Locale.GERMAN),
                        new BigNumber("0,3", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyFactorWithLeadingZerosTest() {
        assertEquals(
                "36",
                BasicMath.multiply(
                        new BigNumber("0012", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyFactorWithTrailingZerosTest() {
        assertEquals(
                "3",
                BasicMath.multiply(
                        new BigNumber("1,500", Locale.GERMAN),
                        new BigNumber("2", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyBothWithLeadingAndTrailingZerosTest() {
        assertEquals(
                "25",
                BasicMath.multiply(
                        new BigNumber("0012,500", Locale.GERMAN),
                        new BigNumber("002,000", Locale.GERMAN)
                ).toString()
        );
    }

    // Sehr große und sehr kleine Zahlen
    @Test
    public void multiplyLargerThanLongTest() {
        assertEquals(
                "18446744073709551616",
                BasicMath.multiply(
                        new BigNumber("4294967296", Locale.GERMAN),
                        new BigNumber("4294967296", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyMuchLargerThanLongTest() {
        assertEquals(
                "340282366920938463463374607431768211456",
                BasicMath.multiply(
                        new BigNumber("18446744073709551616", Locale.GERMAN),
                        new BigNumber("18446744073709551616", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyVeryLargeRepeatedNinesTest() {
        assertEquals(
                "999999999999999999999999999998000000000000000000000000000001",
                BasicMath.multiply(
                        new BigNumber("999999999999999999999999999999", Locale.GERMAN),
                        new BigNumber("999999999999999999999999999999", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyVeryLargeByVerySmallTest() {
        assertEquals(
                "900",
                BasicMath.multiply(
                        new BigNumber("90000000000", Locale.GERMAN),
                        new BigNumber("0,00000001", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyVerySmallByVerySmallTest() {
        assertEquals(
                "0,0000000000000001",
                BasicMath.multiply(
                        new BigNumber("0,00000001", Locale.GERMAN),
                        new BigNumber("0,00000001", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyLongDecimalsTest() {
        assertEquals(
                "2,46913578024691357802",
                BasicMath.multiply(
                        new BigNumber("1,23456789012345678901", Locale.GERMAN),
                        new BigNumber("2", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    // Aufruf über BigNumber
    @Test
    public void multiplyThroughBigNumberMethodTest() {
        assertEquals(
                "7,5",
                new BigNumber("2,5", Locale.GERMAN)
                        .multiply(new BigNumber("3", Locale.GERMAN))
                        .toString(Locale.GERMAN)
        );
    }

    // Locale
    @Test
    public void multiplyWithEnglishLocaleAndNegativeResultTest() {
        assertEquals(
                "-7.5",
                BasicMath.multiply(
                        new BigNumber("-2.5", Locale.US),
                        new BigNumber("3", Locale.US)
                ).toString()
        );
    }

    @Test
    public void multiplyWithEnglishLocaleAndDecimalResultTest() {
        assertEquals(
                "1.25",
                BasicMath.multiply(
                        new BigNumber("2.5", Locale.US),
                        new BigNumber("0.5", Locale.US)
                ).toString()
        );
    }

    @Test
    public void multiplyWithGermanGroupingSeparatorTest() {
        assertEquals(
                "2001",
                BasicMath.multiply(
                        new BigNumber("1.000,5", Locale.GERMAN),
                        new BigNumber("2", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyWithEnglishGroupingSeparatorTest() {
        assertEquals(
                "2001",
                BasicMath.multiply(
                        new BigNumber("1,000.5", Locale.US),
                        new BigNumber("2", Locale.US)
                ).toString()
        );
    }

    @Test
    public void multiplyWithDefaultOutputForGermanInputTest() {
        assertEquals(
                "1.25",
                BasicMath.multiply(
                        new BigNumber("2,5", Locale.GERMAN),
                        new BigNumber("0,5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyWithGermanOutputForEnglishInputTest() {
        assertEquals(
                "1,25",
                BasicMath.multiply(
                        new BigNumber("2.5", Locale.US),
                        new BigNumber("0.5", Locale.US)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyWithFrenchOutputTest() {
        assertEquals(
                "1,25",
                BasicMath.multiply(
                        new BigNumber("2,5", Locale.GERMAN),
                        new BigNumber("0,5", Locale.GERMAN)
                ).toString(Locale.FRENCH)
        );
    }

    // Die Zahlen dürfen in unterschiedlichen Locales angegeben werden
    @Test
    public void multiplyWithGermanAndEnglishLocaleTest() {
        assertEquals(
                "5,25",
                BasicMath.multiply(
                        new BigNumber("3,5", Locale.GERMAN),
                        new BigNumber("1.5", Locale.US)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void multiplyWithEnglishAndGermanLocaleTest() {
        assertEquals(
                "5.25",
                BasicMath.multiply(
                        new BigNumber("1.5", Locale.US),
                        new BigNumber("3,5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyWithLargerSecondFactorInOtherLocaleTest() {
        assertEquals(
                "7",
                BasicMath.multiply(
                        new BigNumber("2", Locale.US),
                        new BigNumber("3,5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void multiplyNegativeWithDifferentLocalesTest() {
        assertEquals(
                "-6,25",
                BasicMath.multiply(
                        new BigNumber("2,5", Locale.GERMAN),
                        new BigNumber("-2.5", Locale.US)
                ).toString(Locale.GERMAN)
        );
    }

    // Eingabewerte dürfen nicht verändert werden

    @Test
    public void multiplyDoesNotChangeFirstFactorTest() {
        final BigNumber factor1 = new BigNumber("5", Locale.GERMAN);
        final BigNumber factor2 = new BigNumber("-8", Locale.GERMAN);

        BasicMath.multiply(factor1, factor2);

        assertEquals("5", factor1.toString());
    }

    @Test
    public void multiplyDoesNotChangeSecondFactorTest() {
        final BigNumber factor1 = new BigNumber("5", Locale.GERMAN);
        final BigNumber factor2 = new BigNumber("-8", Locale.GERMAN);

        BasicMath.multiply(factor1, factor2);

        assertEquals("-8", factor2.toString());
    }

    @Test
    public void multiplyDoesNotChangeNegativeDecimalFirstFactorTest() {
        final BigNumber factor1 = new BigNumber("-7,5", Locale.GERMAN);
        final BigNumber factor2 = new BigNumber("0,25", Locale.GERMAN);

        BasicMath.multiply(factor1, factor2);

        assertEquals("-7,5", factor1.toString(Locale.GERMAN));
    }

    @Test
    public void multiplyDoesNotChangeNegativeDecimalSecondFactorTest() {
        final BigNumber factor1 = new BigNumber("7,5", Locale.GERMAN);
        final BigNumber factor2 = new BigNumber("-0,25", Locale.GERMAN);

        BasicMath.multiply(factor1, factor2);

        assertEquals("-0,25", factor2.toString(Locale.GERMAN));
    }

}
