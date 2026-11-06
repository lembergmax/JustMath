package io.github.lembergmax.justmath.bignumber.math;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.exception.DivisionByZeroException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BasicMathDivideTest {

    @Test
    public void divideBothPositiveSixDigitDecimalsTest() {
        assertEquals(
                "5,3000340202",
                BasicMath.divide(
                        new BigNumber("654,321", Locale.GERMAN),
                        new BigNumber("123,456", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideBothPositiveRepeatedNinesTest() {
        assertEquals(
                "1",
                BasicMath.divide(
                        new BigNumber("999,999", Locale.GERMAN),
                        new BigNumber("999,999", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideBothPositiveZeroDividendTest() {
        assertEquals(
                "0",
                BasicMath.divide(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("123,456", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideBothPositiveVerySmallDecimalsTest() {
        assertEquals(
                "2",
                BasicMath.divide(
                        new BigNumber("0,00000004", Locale.GERMAN),
                        new BigNumber("0,00000002", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideBothPositiveElevenDigitIntegersTest() {
        assertEquals(
                "2",
                BasicMath.divide(
                        new BigNumber("50000000000", Locale.GERMAN),
                        new BigNumber("25000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideBothPositiveDifferentLengthsTest() {
        assertEquals(
                "269,7368421052",
                BasicMath.divide(
                        new BigNumber("123000", Locale.GERMAN),
                        new BigNumber("456", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideBothPositiveTenDigitIntegersTest() {
        assertEquals(
                "8,0000000729",
                BasicMath.divide(
                        new BigNumber("9876543210", Locale.GERMAN),
                        new BigNumber("1234567890", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideBothPositiveVeryLargeIntegersTest() {
        assertEquals(
                "3",
                BasicMath.divide(
                        new BigNumber("90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN),
                        new BigNumber("30000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideFirstNegativeSixDigitDecimalsTest() {
        assertEquals(
                "-5,3000340202",
                BasicMath.divide(
                        new BigNumber("-654,321", Locale.GERMAN),
                        new BigNumber("123,456", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideFirstNegativeRepeatedNinesTest() {
        assertEquals(
                "-1",
                BasicMath.divide(
                        new BigNumber("-999,999", Locale.GERMAN),
                        new BigNumber("999,999", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideFirstNegativeZeroDividendTest() {
        assertEquals(
                "0",
                BasicMath.divide(
                        new BigNumber("-0", Locale.GERMAN),
                        new BigNumber("123,456", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideFirstNegativeVerySmallDecimalsTest() {
        assertEquals(
                "-2",
                BasicMath.divide(
                        new BigNumber("-0,00000004", Locale.GERMAN),
                        new BigNumber("0,00000002", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideFirstNegativeElevenDigitIntegersTest() {
        assertEquals(
                "-2",
                BasicMath.divide(
                        new BigNumber("-50000000000", Locale.GERMAN),
                        new BigNumber("25000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideFirstNegativeDifferentLengthsTest() {
        assertEquals(
                "-269,7368421052",
                BasicMath.divide(
                        new BigNumber("-123000", Locale.GERMAN),
                        new BigNumber("456", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideFirstNegativeTenDigitIntegersTest() {
        assertEquals(
                "-8,0000000729",
                BasicMath.divide(
                        new BigNumber("-9876543210", Locale.GERMAN),
                        new BigNumber("1234567890", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideFirstNegativeVeryLargeIntegersTest() {
        assertEquals(
                "-3",
                BasicMath.divide(
                        new BigNumber("-90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN),
                        new BigNumber("30000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideSecondNegativeSixDigitDecimalsTest() {
        assertEquals(
                "-5,3000340202",
                BasicMath.divide(
                        new BigNumber("654,321", Locale.GERMAN),
                        new BigNumber("-123,456", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideSecondNegativeRepeatedNinesTest() {
        assertEquals(
                "-1",
                BasicMath.divide(
                        new BigNumber("999,999", Locale.GERMAN),
                        new BigNumber("-999,999", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideSecondNegativeZeroDividendTest() {
        assertEquals(
                "0",
                BasicMath.divide(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("-123,456", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideSecondNegativeVerySmallDecimalsTest() {
        assertEquals(
                "-2",
                BasicMath.divide(
                        new BigNumber("0,00000004", Locale.GERMAN),
                        new BigNumber("-0,00000002", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideSecondNegativeElevenDigitIntegersTest() {
        assertEquals(
                "-2",
                BasicMath.divide(
                        new BigNumber("50000000000", Locale.GERMAN),
                        new BigNumber("-25000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideSecondNegativeDifferentLengthsTest() {
        assertEquals(
                "-269,7368421052",
                BasicMath.divide(
                        new BigNumber("123000", Locale.GERMAN),
                        new BigNumber("-456", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideSecondNegativeTenDigitIntegersTest() {
        assertEquals(
                "-8,0000000729",
                BasicMath.divide(
                        new BigNumber("9876543210", Locale.GERMAN),
                        new BigNumber("-1234567890", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideSecondNegativeVeryLargeIntegersTest() {
        assertEquals(
                "-3",
                BasicMath.divide(
                        new BigNumber("90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN),
                        new BigNumber("-30000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideBothNegativeSixDigitDecimalsTest() {
        assertEquals(
                "5,3000340202",
                BasicMath.divide(
                        new BigNumber("-654,321", Locale.GERMAN),
                        new BigNumber("-123,456", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideBothNegativeRepeatedNinesTest() {
        assertEquals(
                "1",
                BasicMath.divide(
                        new BigNumber("-999,999", Locale.GERMAN),
                        new BigNumber("-999,999", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideBothNegativeZeroDividendTest() {
        assertEquals(
                "0",
                BasicMath.divide(
                        new BigNumber("-0", Locale.GERMAN),
                        new BigNumber("-123,456", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideBothNegativeVerySmallDecimalsTest() {
        assertEquals(
                "2",
                BasicMath.divide(
                        new BigNumber("-0,00000004", Locale.GERMAN),
                        new BigNumber("-0,00000002", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideBothNegativeElevenDigitIntegersTest() {
        assertEquals(
                "2",
                BasicMath.divide(
                        new BigNumber("-50000000000", Locale.GERMAN),
                        new BigNumber("-25000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideBothNegativeDifferentLengthsTest() {
        assertEquals(
                "269,7368421052",
                BasicMath.divide(
                        new BigNumber("-123000", Locale.GERMAN),
                        new BigNumber("-456", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideBothNegativeTenDigitIntegersTest() {
        assertEquals(
                "8,0000000729",
                BasicMath.divide(
                        new BigNumber("-9876543210", Locale.GERMAN),
                        new BigNumber("-1234567890", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideBothNegativeVeryLargeIntegersTest() {
        assertEquals(
                "3",
                BasicMath.divide(
                        new BigNumber("-90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN),
                        new BigNumber("-30000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN)
                ).toString()
        );
    }

    // Ganze Zahlen ohne Rest
    @Test
    public void divideSingleDigitsTest() {
        assertEquals(
                "4",
                BasicMath.divide(
                        new BigNumber("8", Locale.GERMAN),
                        new BigNumber("2", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideTwoDigitQuotientTest() {
        assertEquals(
                "12",
                BasicMath.divide(
                        new BigNumber("144", Locale.GERMAN),
                        new BigNumber("12", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideThreeDigitQuotientTest() {
        assertEquals(
                "144",
                BasicMath.divide(
                        new BigNumber("1728", Locale.GERMAN),
                        new BigNumber("12", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideThreeDigitDivisorTest() {
        assertEquals(
                "12",
                BasicMath.divide(
                        new BigNumber("1476", Locale.GERMAN),
                        new BigNumber("123", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideLargestQuotientDigitTest() {
        assertEquals(
                "9",
                BasicMath.divide(
                        new BigNumber("81", Locale.GERMAN),
                        new BigNumber("9", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideQuotientOfOnlyNinesTest() {
        assertEquals(
                "999",
                BasicMath.divide(
                        new BigNumber("8991", Locale.GERMAN),
                        new BigNumber("9", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideQuotientOfOnlyOnesTest() {
        assertEquals(
                "111",
                BasicMath.divide(
                        new BigNumber("1221", Locale.GERMAN),
                        new BigNumber("11", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideByTenTest() {
        assertEquals(
                "12",
                BasicMath.divide(
                        new BigNumber("120", Locale.GERMAN),
                        new BigNumber("10", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideByHundredTest() {
        assertEquals(
                "12",
                BasicMath.divide(
                        new BigNumber("1200", Locale.GERMAN),
                        new BigNumber("100", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideByPowerOfTenWithDecimalResultTest() {
        assertEquals(
                "1,25",
                BasicMath.divide(
                        new BigNumber("125", Locale.GERMAN),
                        new BigNumber("100", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    // Der Rest wird in die nächste Stelle übernommen
    @Test
    public void divideRestCarriedOverMultipleDigitsTest() {
        assertEquals(
                "143",
                BasicMath.divide(
                        new BigNumber("1001", Locale.GERMAN),
                        new BigNumber("7", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideRestCarriedOverEveryDigitTest() {
        assertEquals(
                "101",
                BasicMath.divide(
                        new BigNumber("1111", Locale.GERMAN),
                        new BigNumber("11", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideRestAlmostAsLargeAsDivisorTest() {
        assertEquals(
                "2",
                BasicMath.divide(
                        new BigNumber("998", Locale.GERMAN),
                        new BigNumber("499", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideFirstDigitsSmallerThanDivisorTest() {
        assertEquals(
                "8",
                BasicMath.divide(
                        new BigNumber("1000", Locale.GERMAN),
                        new BigNumber("125", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideRestBecomesZeroBeforeLastDigitTest() {
        assertEquals(
                "100",
                BasicMath.divide(
                        new BigNumber("2400", Locale.GERMAN),
                        new BigNumber("24", Locale.GERMAN)
                ).toString()
        );
    }

    // Nullen im Quotienten
    @Test
    public void divideZeroInMiddleOfQuotientTest() {
        assertEquals(
                "1002",
                BasicMath.divide(
                        new BigNumber("2004", Locale.GERMAN),
                        new BigNumber("2", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideMultipleZerosInMiddleOfQuotientTest() {
        assertEquals(
                "1001",
                BasicMath.divide(
                        new BigNumber("9009", Locale.GERMAN),
                        new BigNumber("9", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideTrailingZerosInQuotientTest() {
        assertEquals(
                "300",
                BasicMath.divide(
                        new BigNumber("1200", Locale.GERMAN),
                        new BigNumber("4", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideResultWithManyTrailingZerosTest() {
        assertEquals(
                "1000000",
                BasicMath.divide(
                        new BigNumber("1000000", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideZeroDirectlyAfterDecimalSeparatorTest() {
        assertEquals(
                "0,05",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("20", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideManyZerosAfterDecimalSeparatorTest() {
        assertEquals(
                "0,000001",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("1000000", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideZeroBetweenDecimalsTest() {
        assertEquals(
                "1,01",
                BasicMath.divide(
                        new BigNumber("101", Locale.GERMAN),
                        new BigNumber("100", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideZerosOnBothSidesOfDecimalSeparatorTest() {
        assertEquals(
                "10,01",
                BasicMath.divide(
                        new BigNumber("40,04", Locale.GERMAN),
                        new BigNumber("4", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    // Ergebnis mit Nachkommastellen
    @Test
    public void divideOneDecimalPlaceTest() {
        assertEquals(
                "3,5",
                BasicMath.divide(
                        new BigNumber("7", Locale.GERMAN),
                        new BigNumber("2", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideOneDecimalPlaceSmallValuesTest() {
        assertEquals(
                "2,5",
                BasicMath.divide(
                        new BigNumber("5", Locale.GERMAN),
                        new BigNumber("2", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideTwoDecimalPlacesTest() {
        assertEquals(
                "2,25",
                BasicMath.divide(
                        new BigNumber("9", Locale.GERMAN),
                        new BigNumber("4", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideThreeDecimalPlacesTest() {
        assertEquals(
                "0,375",
                BasicMath.divide(
                        new BigNumber("3", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideFourDecimalPlacesTest() {
        assertEquals(
                "0,0625",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("16", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideQuotientWithRestContinuesInDecimalsTest() {
        assertEquals(
                "8,0243902439",
                BasicMath.divide(
                        new BigNumber("987", Locale.GERMAN),
                        new BigNumber("123", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideLargerValuesWithDecimalResultTest() {
        assertEquals(
                "12,5",
                BasicMath.divide(
                        new BigNumber("100", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    // Dividend kleiner als Divisor
    @Test
    public void divideDividendSmallerThanDivisorHalfTest() {
        assertEquals(
                "0,5",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("2", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideDividendSmallerThanDivisorQuarterTest() {
        assertEquals(
                "0,75",
                BasicMath.divide(
                        new BigNumber("3", Locale.GERMAN),
                        new BigNumber("4", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideDividendSmallerThanDivisorThousandthTest() {
        assertEquals(
                "0,001",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("1000", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideDividendSmallerThanDivisorSameLengthTest() {
        assertEquals(
                "0,2697368421",
                BasicMath.divide(
                        new BigNumber("123", Locale.GERMAN),
                        new BigNumber("456", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideDividendSmallerThanDivisorByOneTest() {
        assertEquals(
                "0,999",
                BasicMath.divide(
                        new BigNumber("999", Locale.GERMAN),
                        new BigNumber("1000", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideDividendMuchSmallerThanDivisorTest() {
        assertEquals(
                "0,0000000081",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("123456789", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    // Dezimalzahlen
    @Test
    public void divideBothDecimalsWithIntegerResultTest() {
        assertEquals(
                "6",
                BasicMath.divide(
                        new BigNumber("1,5", Locale.GERMAN),
                        new BigNumber("0,25", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideBothDecimalsSameLengthTest() {
        assertEquals(
                "3",
                BasicMath.divide(
                        new BigNumber("7,5", Locale.GERMAN),
                        new BigNumber("2,5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideBothDecimalsSmallerThanOneTest() {
        assertEquals(
                "2",
                BasicMath.divide(
                        new BigNumber("0,5", Locale.GERMAN),
                        new BigNumber("0,25", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideBothDecimalsWithDecimalResultTest() {
        assertEquals(
                "0,5",
                BasicMath.divide(
                        new BigNumber("0,1", Locale.GERMAN),
                        new BigNumber("0,2", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideBothDecimalsThreeDecimalPlacesTest() {
        assertEquals(
                "3",
                BasicMath.divide(
                        new BigNumber("3,75", Locale.GERMAN),
                        new BigNumber("1,25", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideDecimalDividendSmallerThanDivisorTest() {
        assertEquals(
                "0,01",
                BasicMath.divide(
                        new BigNumber("0,001", Locale.GERMAN),
                        new BigNumber("0,1", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideIntegerByDecimalTest() {
        assertEquals(
                "12",
                BasicMath.divide(
                        new BigNumber("3", Locale.GERMAN),
                        new BigNumber("0,25", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideIntegerByDecimalSmallerThanOneTest() {
        assertEquals(
                "20",
                BasicMath.divide(
                        new BigNumber("10", Locale.GERMAN),
                        new BigNumber("0,5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideIntegerByVerySmallDecimalTest() {
        assertEquals(
                "100000",
                BasicMath.divide(
                        new BigNumber("100", Locale.GERMAN),
                        new BigNumber("0,001", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideDecimalByIntegerTest() {
        assertEquals(
                "0,5",
                BasicMath.divide(
                        new BigNumber("2,5", Locale.GERMAN),
                        new BigNumber("5", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideDecimalByIntegerSmallerThanOneTest() {
        assertEquals(
                "0,25",
                BasicMath.divide(
                        new BigNumber("0,75", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideDecimalByLargerIntegerTest() {
        assertEquals(
                "0,0005",
                BasicMath.divide(
                        new BigNumber("0,5", Locale.GERMAN),
                        new BigNumber("1000", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    // Unterschiedliche Längen
    @Test
    public void divideLongDividendShortDivisorTest() {
        assertEquals(
                "41152263",
                BasicMath.divide(
                        new BigNumber("123456789", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideShortDividendLongDivisorTest() {
        assertEquals(
                "0,0000000243",
                BasicMath.divide(
                        new BigNumber("3", Locale.GERMAN),
                        new BigNumber("123456789", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideDifferentAfterDecimalLengthsTest() {
        assertEquals(
                "12",
                BasicMath.divide(
                        new BigNumber("1,5", Locale.GERMAN),
                        new BigNumber("0,125", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideDifferentAfterDecimalLengthsLongerDividendTest() {
        assertEquals(
                "0,05",
                BasicMath.divide(
                        new BigNumber("0,125", Locale.GERMAN),
                        new BigNumber("2,5", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideDifferentBeforeDecimalLengthsTest() {
        assertEquals(
                "2001",
                BasicMath.divide(
                        new BigNumber("1000,5", Locale.GERMAN),
                        new BigNumber("0,5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideDifferentBeforeDecimalLengthsLongerDivisorTest() {
        assertEquals(
                "0,0004997501",
                BasicMath.divide(
                        new BigNumber("0,5", Locale.GERMAN),
                        new BigNumber("1000,5", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    // Das Ergebnis wird nach 10 Nachkommastellen abgeschnitten, nicht gerundet
    @Test
    public void divideCutOffOneThirdTest() {
        assertEquals(
                "0,3333333333",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideCutOffTwoThirdsIsNotRoundedUpTest() {
        assertEquals(
                "0,6666666666",
                BasicMath.divide(
                        new BigNumber("2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideCutOffOneSixthTest() {
        assertEquals(
                "0,1666666666",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("6", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideCutOffFiveSixthsTest() {
        assertEquals(
                "0,8333333333",
                BasicMath.divide(
                        new BigNumber("5", Locale.GERMAN),
                        new BigNumber("6", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideCutOffOneSeventhTest() {
        assertEquals(
                "0,1428571428",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("7", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideCutOffOneNinthTest() {
        assertEquals(
                "0,1111111111",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("9", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideCutOffEightNinthsIsNotRoundedUpTest() {
        assertEquals(
                "0,8888888888",
                BasicMath.divide(
                        new BigNumber("8", Locale.GERMAN),
                        new BigNumber("9", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideCutOffOneEleventhTest() {
        assertEquals(
                "0,0909090909",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("11", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideCutOffOneThirteenthTest() {
        assertEquals(
                "0,0769230769",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("13", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideCutOffWithIntegerPartTest() {
        assertEquals(
                "3,3333333333",
                BasicMath.divide(
                        new BigNumber("10", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideCutOffApproximationOfPiTest() {
        assertEquals(
                "3,1428571428",
                BasicMath.divide(
                        new BigNumber("22", Locale.GERMAN),
                        new BigNumber("7", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideCutOffNegativeTwoThirdsIsNotRoundedUpTest() {
        assertEquals(
                "-0,6666666666",
                BasicMath.divide(
                        new BigNumber("-2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideCutOffNegativeDivisorTwoThirdsIsNotRoundedUpTest() {
        assertEquals(
                "-0,6666666666",
                BasicMath.divide(
                        new BigNumber("2", Locale.GERMAN),
                        new BigNumber("-3", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideCutOffBothNegativeTwoThirdsIsNotRoundedUpTest() {
        assertEquals(
                "0,6666666666",
                BasicMath.divide(
                        new BigNumber("-2", Locale.GERMAN),
                        new BigNumber("-3", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideCutOffDecimalsOfDividendTest() {
        assertEquals(
                "0,1234567899",
                BasicMath.divide(
                        new BigNumber("0,12345678999", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideCutOffRemovesTrailingZeroOfResultTest() {
        assertEquals(
                "0,123456789",
                BasicMath.divide(
                        new BigNumber("0,12345678901", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    // Grenzen der Nachkommastellen
    @Test
    public void divideExactlyTenDecimalPlacesTest() {
        assertEquals(
                "0,0009765625",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("1024", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideElevenDecimalPlacesAreCutOffTest() {
        assertEquals(
                "0,0004882812",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("2048", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideSmallestPossibleResultTest() {
        assertEquals(
                "0,0000000001",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("10000000000", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideBelowSmallestPossibleResultIsZeroTest() {
        assertEquals(
                "0",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("100000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideJustBelowSmallestPossibleResultIsZeroTest() {
        assertEquals(
                "0",
                BasicMath.divide(
                        new BigNumber("9", Locale.GERMAN),
                        new BigNumber("100000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideVerySmallDividendBelowSmallestPossibleResultIsZeroTest() {
        assertEquals(
                "0",
                BasicMath.divide(
                        new BigNumber("0,00000000001", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideSmallestPossibleDividendStaysAfterDivisionByOneTest() {
        assertEquals(
                "0,0000000001",
                BasicMath.divide(
                        new BigNumber("0,0000000001", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideTenDecimalPlacesWithIntegerPartTest() {
        assertEquals(
                "12345,0000000001",
                BasicMath.divide(
                        new BigNumber("12345,0000000001", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    // Division durch 0
    @Test
    public void divideByZeroTest() {
        assertThrows(
                DivisionByZeroException.class,
                () -> BasicMath.divide(
                        new BigNumber("5", Locale.GERMAN),
                        new BigNumber("0", Locale.GERMAN)
                )
        );
    }

    @Test
    public void divideNegativeByZeroTest() {
        assertThrows(
                DivisionByZeroException.class,
                () -> BasicMath.divide(
                        new BigNumber("-5", Locale.GERMAN),
                        new BigNumber("0", Locale.GERMAN)
                )
        );
    }

    @Test
    public void divideDecimalByZeroTest() {
        assertThrows(
                DivisionByZeroException.class,
                () -> BasicMath.divide(
                        new BigNumber("0,5", Locale.GERMAN),
                        new BigNumber("0", Locale.GERMAN)
                )
        );
    }

    @Test
    public void divideZeroByZeroTest() {
        assertThrows(
                DivisionByZeroException.class,
                () -> BasicMath.divide(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("0", Locale.GERMAN)
                )
        );
    }

    @Test
    public void divideNegativeZeroByZeroTest() {
        assertThrows(
                DivisionByZeroException.class,
                () -> BasicMath.divide(
                        new BigNumber("-0", Locale.GERMAN),
                        new BigNumber("0", Locale.GERMAN)
                )
        );
    }

    @Test
    public void divideZeroByNegativeZeroTest() {
        assertThrows(
                DivisionByZeroException.class,
                () -> BasicMath.divide(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("-0", Locale.GERMAN)
                )
        );
    }

    @Test
    public void divideNegativeZeroByNegativeZeroTest() {
        assertThrows(
                DivisionByZeroException.class,
                () -> BasicMath.divide(
                        new BigNumber("-0", Locale.GERMAN),
                        new BigNumber("-0", Locale.GERMAN)
                )
        );
    }

    @Test
    public void divideByNegativeZeroTest() {
        assertThrows(
                DivisionByZeroException.class,
                () -> BasicMath.divide(
                        new BigNumber("5", Locale.GERMAN),
                        new BigNumber("-0", Locale.GERMAN)
                )
        );
    }

    @Test
    public void divideByZeroWithDecimalPlaceTest() {
        assertThrows(
                DivisionByZeroException.class,
                () -> BasicMath.divide(
                        new BigNumber("5", Locale.GERMAN),
                        new BigNumber("0,0", Locale.GERMAN)
                )
        );
    }

    @Test
    public void divideByZeroWithManyDecimalPlacesTest() {
        assertThrows(
                DivisionByZeroException.class,
                () -> BasicMath.divide(
                        new BigNumber("5", Locale.GERMAN),
                        new BigNumber("0,000000", Locale.GERMAN)
                )
        );
    }

    @Test
    public void divideByZeroWithLeadingZerosTest() {
        assertThrows(
                DivisionByZeroException.class,
                () -> BasicMath.divide(
                        new BigNumber("5", Locale.GERMAN),
                        new BigNumber("000", Locale.GERMAN)
                )
        );
    }

    @Test
    public void divideByZeroWithPositiveSignTest() {
        assertThrows(
                DivisionByZeroException.class,
                () -> BasicMath.divide(
                        new BigNumber("5", Locale.GERMAN),
                        new BigNumber("+0", Locale.GERMAN)
                )
        );
    }

    @Test
    public void divideVeryLargeByZeroTest() {
        assertThrows(
                DivisionByZeroException.class,
                () -> BasicMath.divide(
                        new BigNumber("90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN),
                        new BigNumber("0", Locale.GERMAN)
                )
        );
    }

    @Test
    public void divideVerySmallByZeroTest() {
        assertThrows(
                DivisionByZeroException.class,
                () -> BasicMath.divide(
                        new BigNumber("0,00000001", Locale.GERMAN),
                        new BigNumber("0", Locale.GERMAN)
                )
        );
    }

    @Test
    public void divideByZeroWithEnglishLocaleTest() {
        assertThrows(
                DivisionByZeroException.class,
                () -> BasicMath.divide(
                        new BigNumber("5.5", Locale.US),
                        new BigNumber("0.0", Locale.US)
                )
        );
    }

    // Auch eine mit negate() erzeugte 0 muss als Division durch 0 erkannt werden.
    // Sonst läuft die Schleife endlos, deshalb bricht der Test nach 5 Sekunden ab.
    @Test
    public void divideByNegatedZeroTest() {
        assertTimeoutPreemptively(
                Duration.ofSeconds(5),
                () -> assertThrows(
                        DivisionByZeroException.class,
                        () -> BasicMath.divide(
                                new BigNumber("5", Locale.GERMAN),
                                new BigNumber("0", Locale.GERMAN).negate()
                        )
                )
        );
    }

    @Test
    public void divideNegatedZeroByPositiveTest() {
        assertEquals(
                "0",
                BasicMath.divide(
                        new BigNumber("0", Locale.GERMAN).negate(),
                        new BigNumber("5", Locale.GERMAN)
                ).toString()
        );
    }

    // Null als Dividend
    @Test
    public void divideZeroByPositiveTest() {
        assertEquals(
                "0",
                BasicMath.divide(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideZeroByNegativeTest() {
        assertEquals(
                "0",
                BasicMath.divide(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("-5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideNegativeZeroByPositiveTest() {
        assertEquals(
                "0",
                BasicMath.divide(
                        new BigNumber("-0", Locale.GERMAN),
                        new BigNumber("5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideNegativeZeroByNegativeTest() {
        assertEquals(
                "0",
                BasicMath.divide(
                        new BigNumber("-0", Locale.GERMAN),
                        new BigNumber("-5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideZeroWithDecimalPlacesByPositiveTest() {
        assertEquals(
                "0",
                BasicMath.divide(
                        new BigNumber("0,000", Locale.GERMAN),
                        new BigNumber("5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideZeroWithLeadingZerosByPositiveTest() {
        assertEquals(
                "0",
                BasicMath.divide(
                        new BigNumber("000", Locale.GERMAN),
                        new BigNumber("5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideZeroByVerySmallDecimalTest() {
        assertEquals(
                "0",
                BasicMath.divide(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("0,00000001", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideZeroByVeryLargeIntegerTest() {
        assertEquals(
                "0",
                BasicMath.divide(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("30000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideZeroByNegativeResultIsNotNegativeTest() {
        assertFalse(
                BasicMath.divide(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("-5", Locale.GERMAN)
                ).isNegative()
        );
    }

    @Test
    public void divideNegativeZeroByPositiveResultIsNotNegativeTest() {
        assertFalse(
                BasicMath.divide(
                        new BigNumber("-0", Locale.GERMAN),
                        new BigNumber("5", Locale.GERMAN)
                ).isNegative()
        );
    }

    @Test
    public void divideNegativeZeroByNegativeResultIsNotNegativeTest() {
        assertFalse(
                BasicMath.divide(
                        new BigNumber("-0", Locale.GERMAN),
                        new BigNumber("-5", Locale.GERMAN)
                ).isNegative()
        );
    }

    // Division durch 1 und -1
    @Test
    public void divideIntegerByOneTest() {
        assertEquals(
                "12345",
                BasicMath.divide(
                        new BigNumber("12345", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideNegativeIntegerByOneTest() {
        assertEquals(
                "-12345",
                BasicMath.divide(
                        new BigNumber("-12345", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideIntegerByNegativeOneTest() {
        assertEquals(
                "-12345",
                BasicMath.divide(
                        new BigNumber("12345", Locale.GERMAN),
                        new BigNumber("-1", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideNegativeIntegerByNegativeOneTest() {
        assertEquals(
                "12345",
                BasicMath.divide(
                        new BigNumber("-12345", Locale.GERMAN),
                        new BigNumber("-1", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideDecimalByOneTest() {
        assertEquals(
                "2,5",
                BasicMath.divide(
                        new BigNumber("2,5", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideNegativeDecimalByOneTest() {
        assertEquals(
                "-2,5",
                BasicMath.divide(
                        new BigNumber("-2,5", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideDecimalByNegativeOneTest() {
        assertEquals(
                "-2,5",
                BasicMath.divide(
                        new BigNumber("2,5", Locale.GERMAN),
                        new BigNumber("-1", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideDecimalSmallerThanOneByOneTest() {
        assertEquals(
                "0,5",
                BasicMath.divide(
                        new BigNumber("0,5", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideByOneWithDecimalPlaceTest() {
        assertEquals(
                "12345",
                BasicMath.divide(
                        new BigNumber("12345", Locale.GERMAN),
                        new BigNumber("1,0", Locale.GERMAN)
                ).toString()
        );
    }

    // Gleiche Werte ergeben 1 oder -1
    @Test
    public void divideEqualIntegersTest() {
        assertEquals(
                "1",
                BasicMath.divide(
                        new BigNumber("12", Locale.GERMAN),
                        new BigNumber("12", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideEqualSingleDigitsTest() {
        assertEquals(
                "1",
                BasicMath.divide(
                        new BigNumber("7", Locale.GERMAN),
                        new BigNumber("7", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideEqualValuesFirstNegativeTest() {
        assertEquals(
                "-1",
                BasicMath.divide(
                        new BigNumber("-7", Locale.GERMAN),
                        new BigNumber("7", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideEqualValuesSecondNegativeTest() {
        assertEquals(
                "-1",
                BasicMath.divide(
                        new BigNumber("7", Locale.GERMAN),
                        new BigNumber("-7", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideEqualValuesBothNegativeTest() {
        assertEquals(
                "1",
                BasicMath.divide(
                        new BigNumber("-7", Locale.GERMAN),
                        new BigNumber("-7", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideEqualDecimalsTest() {
        assertEquals(
                "1",
                BasicMath.divide(
                        new BigNumber("2,5", Locale.GERMAN),
                        new BigNumber("2,5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideEqualDecimalsSmallerThanOneTest() {
        assertEquals(
                "1",
                BasicMath.divide(
                        new BigNumber("0,00000001", Locale.GERMAN),
                        new BigNumber("0,00000001", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideEqualValuesWithDifferentDecimalLengthsTest() {
        assertEquals(
                "1",
                BasicMath.divide(
                        new BigNumber("2,50", Locale.GERMAN),
                        new BigNumber("2,5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideEqualValuesWithLeadingZerosTest() {
        assertEquals(
                "1",
                BasicMath.divide(
                        new BigNumber("007", Locale.GERMAN),
                        new BigNumber("7", Locale.GERMAN)
                ).toString()
        );
    }

    // Vorzeichen des Ergebnisses
    @Test
    public void divideBothPositiveResultIsNotNegativeTest() {
        assertFalse(
                BasicMath.divide(
                        new BigNumber("8", Locale.GERMAN),
                        new BigNumber("2", Locale.GERMAN)
                ).isNegative()
        );
    }

    @Test
    public void divideFirstNegativeResultIsNegativeTest() {
        assertTrue(
                BasicMath.divide(
                        new BigNumber("-8", Locale.GERMAN),
                        new BigNumber("2", Locale.GERMAN)
                ).isNegative()
        );
    }

    @Test
    public void divideSecondNegativeResultIsNegativeTest() {
        assertTrue(
                BasicMath.divide(
                        new BigNumber("8", Locale.GERMAN),
                        new BigNumber("-2", Locale.GERMAN)
                ).isNegative()
        );
    }

    @Test
    public void divideBothNegativeResultIsNotNegativeTest() {
        assertFalse(
                BasicMath.divide(
                        new BigNumber("-8", Locale.GERMAN),
                        new BigNumber("-2", Locale.GERMAN)
                ).isNegative()
        );
    }

    @Test
    public void divideNegativeDecimalResultIsNegativeTest() {
        assertTrue(
                BasicMath.divide(
                        new BigNumber("-1", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN)
                ).isNegative()
        );
    }

    @Test
    public void divideNegativeByPositiveTest() {
        assertEquals(
                "-4",
                BasicMath.divide(
                        new BigNumber("-8", Locale.GERMAN),
                        new BigNumber("2", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void dividePositiveByNegativeTest() {
        assertEquals(
                "-4",
                BasicMath.divide(
                        new BigNumber("8", Locale.GERMAN),
                        new BigNumber("-2", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideNegativeByNegativeTest() {
        assertEquals(
                "4",
                BasicMath.divide(
                        new BigNumber("-8", Locale.GERMAN),
                        new BigNumber("-2", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideNegativeDecimalByPositiveDecimalTest() {
        assertEquals(
                "-3",
                BasicMath.divide(
                        new BigNumber("-7,5", Locale.GERMAN),
                        new BigNumber("2,5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void dividePositiveDecimalByNegativeDecimalTest() {
        assertEquals(
                "-3",
                BasicMath.divide(
                        new BigNumber("7,5", Locale.GERMAN),
                        new BigNumber("-2,5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideNegativeDecimalByNegativeDecimalTest() {
        assertEquals(
                "3",
                BasicMath.divide(
                        new BigNumber("-7,5", Locale.GERMAN),
                        new BigNumber("-2,5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideNegativeByPositiveWithDecimalResultTest() {
        assertEquals(
                "-0,25",
                BasicMath.divide(
                        new BigNumber("-1", Locale.GERMAN),
                        new BigNumber("4", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void dividePositiveByNegativeWithDecimalResultTest() {
        assertEquals(
                "-0,25",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("-4", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideNegativeByNegativeWithDecimalResultTest() {
        assertEquals(
                "0,25",
                BasicMath.divide(
                        new BigNumber("-1", Locale.GERMAN),
                        new BigNumber("-4", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideWithExplicitPositiveSignsTest() {
        assertEquals(
                "4",
                BasicMath.divide(
                        new BigNumber("+8", Locale.GERMAN),
                        new BigNumber("+2", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideWithExplicitPositiveSignAndNegativeDivisorTest() {
        assertEquals(
                "-4",
                BasicMath.divide(
                        new BigNumber("+8", Locale.GERMAN),
                        new BigNumber("-2", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideWithNegativeDividendAndExplicitPositiveSignTest() {
        assertEquals(
                "-4",
                BasicMath.divide(
                        new BigNumber("-8", Locale.GERMAN),
                        new BigNumber("+2", Locale.GERMAN)
                ).toString()
        );
    }

    // Ist das Ergebnis nach dem Abschneiden 0, ist es nicht negativ
    @Test
    public void divideNegativeBelowSmallestPossibleResultIsZeroTest() {
        assertEquals(
                "0",
                BasicMath.divide(
                        new BigNumber("-1", Locale.GERMAN),
                        new BigNumber("100000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideNegativeDivisorBelowSmallestPossibleResultIsZeroTest() {
        assertEquals(
                "0",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("-100000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideNegativeBelowSmallestPossibleResultIsNotNegativeTest() {
        assertFalse(
                BasicMath.divide(
                        new BigNumber("-1", Locale.GERMAN),
                        new BigNumber("100000000000", Locale.GERMAN)
                ).isNegative()
        );
    }

    @Test
    public void divideNegativeDivisorBelowSmallestPossibleResultIsNotNegativeTest() {
        assertFalse(
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("-100000000000", Locale.GERMAN)
                ).isNegative()
        );
    }

    // Normalisierung des Ergebnisses
    @Test
    public void divideResultWithoutDecimalsTest() {
        assertEquals(
                "2",
                BasicMath.divide(
                        new BigNumber("6", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideResultWithTrailingZeroRemovedTest() {
        assertEquals(
                "2,5",
                BasicMath.divide(
                        new BigNumber("10", Locale.GERMAN),
                        new BigNumber("4", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideResultWithoutLeadingZerosTest() {
        assertEquals(
                "100",
                BasicMath.divide(
                        new BigNumber("1000", Locale.GERMAN),
                        new BigNumber("10", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideResultBelowOneHasOneLeadingZeroTest() {
        assertEquals(
                "0,6",
                BasicMath.divide(
                        new BigNumber("3", Locale.GERMAN),
                        new BigNumber("5", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideDividendWithLeadingZerosTest() {
        assertEquals(
                "12",
                BasicMath.divide(
                        new BigNumber("00144", Locale.GERMAN),
                        new BigNumber("12", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideDivisorWithLeadingZerosTest() {
        assertEquals(
                "12",
                BasicMath.divide(
                        new BigNumber("144", Locale.GERMAN),
                        new BigNumber("0012", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideDividendWithTrailingZerosTest() {
        assertEquals(
                "3",
                BasicMath.divide(
                        new BigNumber("1,500", Locale.GERMAN),
                        new BigNumber("0,5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideDivisorWithTrailingZerosTest() {
        assertEquals(
                "3",
                BasicMath.divide(
                        new BigNumber("1,5", Locale.GERMAN),
                        new BigNumber("0,500", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideBothWithLeadingAndTrailingZerosTest() {
        assertEquals(
                "5",
                BasicMath.divide(
                        new BigNumber("0012,500", Locale.GERMAN),
                        new BigNumber("002,500", Locale.GERMAN)
                ).toString()
        );
    }

    // Sehr große und sehr kleine Zahlen
    @Test
    public void divideEighteenDigitIntegersTest() {
        assertEquals(
                "8,0000000729",
                BasicMath.divide(
                        new BigNumber("987654321987654321", Locale.GERMAN),
                        new BigNumber("123456789123456789", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideLargerThanLongTest() {
        assertEquals(
                "4294967296",
                BasicMath.divide(
                        new BigNumber("18446744073709551616", Locale.GERMAN),
                        new BigNumber("4294967296", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideMuchLargerThanLongTest() {
        assertEquals(
                "18446744073709551616",
                BasicMath.divide(
                        new BigNumber("340282366920938463463374607431768211456", Locale.GERMAN),
                        new BigNumber("18446744073709551616", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideVeryLargeRepeatedNinesTest() {
        assertEquals(
                "333333333333333333333333333333",
                BasicMath.divide(
                        new BigNumber("999999999999999999999999999999", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideVeryLargeWithDecimalResultTest() {
        assertEquals(
                "333333333333333333333333333333333333333,3333333333",
                BasicMath.divide(
                        new BigNumber("1000000000000000000000000000000000000000", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideVeryLargeByVeryLargeWithDecimalResultTest() {
        assertEquals(
                "1,4285714285",
                BasicMath.divide(
                        new BigNumber("1000000000000000000000000000000", Locale.GERMAN),
                        new BigNumber("700000000000000000000000000000", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideVeryLargeByVerySmallTest() {
        assertEquals(
                "9000000000000000000",
                BasicMath.divide(
                        new BigNumber("90000000000", Locale.GERMAN),
                        new BigNumber("0,00000001", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideVerySmallByVeryLargeIsZeroTest() {
        assertEquals(
                "0",
                BasicMath.divide(
                        new BigNumber("0,00000001", Locale.GERMAN),
                        new BigNumber("90000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideVerySmallBySmallerValueTest() {
        assertEquals(
                "2",
                BasicMath.divide(
                        new BigNumber("0,00000002", Locale.GERMAN),
                        new BigNumber("0,00000001", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideVerySmallBySmallValueWithDecimalResultTest() {
        assertEquals(
                "0,3333333333",
                BasicMath.divide(
                        new BigNumber("0,00000001", Locale.GERMAN),
                        new BigNumber("0,00000003", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideOneByVeryLargeIsZeroTest() {
        assertEquals(
                "0",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("1000000000000000000000000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideLongDecimalsEqualValuesTest() {
        assertEquals(
                "1",
                BasicMath.divide(
                        new BigNumber("1,23456789012345678901", Locale.GERMAN),
                        new BigNumber("1,23456789012345678901", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideLongDecimalsWithIntegerResultTest() {
        assertEquals(
                "2",
                BasicMath.divide(
                        new BigNumber("2,46913578024691357802", Locale.GERMAN),
                        new BigNumber("1,23456789012345678901", Locale.GERMAN)
                ).toString()
        );
    }

    // Division als Umkehrung der Multiplikation
    @Test
    public void divideReversesMultiplyIntegersTest() {
        final BigNumber factor1 = new BigNumber("12345", Locale.GERMAN);
        final BigNumber factor2 = new BigNumber("6789", Locale.GERMAN);

        assertEquals(
                "12345",
                BasicMath.divide(BasicMath.multiply(factor1, factor2), factor2).toString()
        );
    }

    @Test
    public void divideReversesMultiplyDecimalsTest() {
        final BigNumber factor1 = new BigNumber("2,5", Locale.GERMAN);
        final BigNumber factor2 = new BigNumber("0,4", Locale.GERMAN);

        assertEquals(
                "2,5",
                BasicMath.divide(BasicMath.multiply(factor1, factor2), factor2).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideReversesMultiplyNegativeFactorTest() {
        final BigNumber factor1 = new BigNumber("-12345", Locale.GERMAN);
        final BigNumber factor2 = new BigNumber("6789", Locale.GERMAN);

        assertEquals(
                "-12345",
                BasicMath.divide(BasicMath.multiply(factor1, factor2), factor2).toString()
        );
    }

    @Test
    public void divideReversesMultiplyNegativeDivisorTest() {
        final BigNumber factor1 = new BigNumber("12345", Locale.GERMAN);
        final BigNumber factor2 = new BigNumber("-6789", Locale.GERMAN);

        assertEquals(
                "12345",
                BasicMath.divide(BasicMath.multiply(factor1, factor2), factor2).toString()
        );
    }

    @Test
    public void divideReversesMultiplyVeryLargeIntegersTest() {
        final BigNumber factor1 = new BigNumber("98765432109876543210", Locale.GERMAN);
        final BigNumber factor2 = new BigNumber("12345678901234567890", Locale.GERMAN);

        assertEquals(
                "98765432109876543210",
                BasicMath.divide(BasicMath.multiply(factor1, factor2), factor2).toString()
        );
    }

    // Das Ergebnis kann weiterverwendet werden
    @Test
    public void divideResultCanBeDividedAgainTest() {
        assertEquals(
                "10",
                BasicMath.divide(
                        BasicMath.divide(
                                new BigNumber("100", Locale.GERMAN),
                                new BigNumber("2", Locale.GERMAN)
                        ),
                        new BigNumber("5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideCutOffResultMultipliedDoesNotReachOriginalValueTest() {
        assertEquals(
                "0,9999999999",
                BasicMath.multiply(
                        BasicMath.divide(
                                new BigNumber("1", Locale.GERMAN),
                                new BigNumber("3", Locale.GERMAN)
                        ),
                        new BigNumber("3", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    // Aufruf über BigNumber
    @Test
    public void divideThroughBigNumberMethodTest() {
        assertEquals(
                "3,75",
                new BigNumber("7,5", Locale.GERMAN)
                        .divide(new BigNumber("2", Locale.GERMAN))
                        .toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideByZeroThroughBigNumberMethodTest() {
        assertThrows(
                DivisionByZeroException.class,
                () -> new BigNumber("5", Locale.GERMAN).divide(new BigNumber("0", Locale.GERMAN))
        );
    }

    // Locale
    @Test
    public void divideWithEnglishLocaleTest() {
        assertEquals(
                "3.75",
                BasicMath.divide(
                        new BigNumber("7.5", Locale.US),
                        new BigNumber("2", Locale.US)
                ).toString()
        );
    }

    @Test
    public void divideWithEnglishLocaleAndNegativeResultTest() {
        assertEquals(
                "-3.75",
                BasicMath.divide(
                        new BigNumber("-7.5", Locale.US),
                        new BigNumber("2", Locale.US)
                ).toString()
        );
    }

    @Test
    public void divideWithEnglishLocaleAndIntegerResultTest() {
        assertEquals(
                "3",
                BasicMath.divide(
                        new BigNumber("7.5", Locale.US),
                        new BigNumber("2.5", Locale.US)
                ).toString()
        );
    }

    @Test
    public void divideWithGermanGroupingSeparatorTest() {
        assertEquals(
                "2001",
                BasicMath.divide(
                        new BigNumber("1.000,5", Locale.GERMAN),
                        new BigNumber("0,5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideWithEnglishGroupingSeparatorTest() {
        assertEquals(
                "2001",
                BasicMath.divide(
                        new BigNumber("1,000.5", Locale.US),
                        new BigNumber("0.5", Locale.US)
                ).toString()
        );
    }

    @Test
    public void divideWithGroupingSeparatorInDivisorTest() {
        assertEquals(
                "1,9990004997",
                BasicMath.divide(
                        new BigNumber("2.000", Locale.GERMAN),
                        new BigNumber("1.000,5", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideWithDefaultOutputForGermanInputTest() {
        assertEquals(
                "3.75",
                BasicMath.divide(
                        new BigNumber("7,5", Locale.GERMAN),
                        new BigNumber("2", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideWithGermanOutputForEnglishInputTest() {
        assertEquals(
                "3,75",
                BasicMath.divide(
                        new BigNumber("7.5", Locale.US),
                        new BigNumber("2", Locale.US)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideWithFrenchOutputTest() {
        assertEquals(
                "3,75",
                BasicMath.divide(
                        new BigNumber("7,5", Locale.GERMAN),
                        new BigNumber("2", Locale.GERMAN)
                ).toString(Locale.FRENCH)
        );
    }

    // Die Zahlen dürfen in unterschiedlichen Locales angegeben werden
    @Test
    public void divideWithGermanAndEnglishLocaleTest() {
        assertEquals(
                "2",
                BasicMath.divide(
                        new BigNumber("3,5", Locale.GERMAN),
                        new BigNumber("1.75", Locale.US)
                ).toString()
        );
    }

    @Test
    public void divideWithEnglishAndGermanLocaleTest() {
        assertEquals(
                "2.5",
                BasicMath.divide(
                        new BigNumber("1.25", Locale.US),
                        new BigNumber("0,5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideWithLargerSecondOperandInOtherLocaleTest() {
        assertEquals(
                "0.2857142857",
                BasicMath.divide(
                        new BigNumber("1", Locale.US),
                        new BigNumber("3,5", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideEqualValuesWithDifferentLocalesTest() {
        assertEquals(
                "1",
                BasicMath.divide(
                        new BigNumber("2,5", Locale.GERMAN),
                        new BigNumber("2.5", Locale.US)
                ).toString()
        );
    }

    @Test
    public void divideNegativeWithDifferentLocalesTest() {
        assertEquals(
                "-1",
                BasicMath.divide(
                        new BigNumber("2,5", Locale.GERMAN),
                        new BigNumber("-2.5", Locale.US)
                ).toString()
        );
    }

    // Eingabewerte dürfen nicht verändert werden
    @Test
    public void divideDoesNotChangeDividendTest() {
        final BigNumber dividend = new BigNumber("5", Locale.GERMAN);
        final BigNumber divisor = new BigNumber("-8", Locale.GERMAN);

        BasicMath.divide(dividend, divisor);

        assertEquals("5", dividend.toString());
    }

    @Test
    public void divideDoesNotChangeDivisorTest() {
        final BigNumber dividend = new BigNumber("5", Locale.GERMAN);
        final BigNumber divisor = new BigNumber("-8", Locale.GERMAN);

        BasicMath.divide(dividend, divisor);

        assertEquals("-8", divisor.toString());
    }

    @Test
    public void divideDoesNotChangeNegativeDecimalDividendTest() {
        final BigNumber dividend = new BigNumber("-7,5", Locale.GERMAN);
        final BigNumber divisor = new BigNumber("0,25", Locale.GERMAN);

        BasicMath.divide(dividend, divisor);

        assertEquals("-7,5", dividend.toString(Locale.GERMAN));
    }

    @Test
    public void divideDoesNotChangeNegativeDecimalDivisorTest() {
        final BigNumber dividend = new BigNumber("7,5", Locale.GERMAN);
        final BigNumber divisor = new BigNumber("-0,25", Locale.GERMAN);

        BasicMath.divide(dividend, divisor);

        assertEquals("-0,25", divisor.toString(Locale.GERMAN));
    }

    @Test
    public void divideZeroDividendDoesNotChangeDivisorTest() {
        final BigNumber dividend = new BigNumber("0", Locale.GERMAN);
        final BigNumber divisor = new BigNumber("-8", Locale.GERMAN);

        BasicMath.divide(dividend, divisor);

        assertEquals("-8", divisor.toString());
    }

}
