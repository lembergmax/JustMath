package io.github.lembergmax.justmath.bignumber.math;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.exception.DivisionByZeroException;
import io.github.lembergmax.justmath.exception.RoundingIsNecessaryException;
import org.junit.jupiter.api.Test;

import java.math.MathContext;
import java.math.RoundingMode;
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
                "5,3000340202177294",
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
                "269,7368421052631579",
                BasicMath.divide(
                        new BigNumber("123000", Locale.GERMAN),
                        new BigNumber("456", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideBothPositiveTenDigitIntegersTest() {
        assertEquals(
                "8,0000000729000007",
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
                "-5,3000340202177294",
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
                "-269,7368421052631579",
                BasicMath.divide(
                        new BigNumber("-123000", Locale.GERMAN),
                        new BigNumber("456", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideFirstNegativeTenDigitIntegersTest() {
        assertEquals(
                "-8,0000000729000007",
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
                "-5,3000340202177294",
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
                "-269,7368421052631579",
                BasicMath.divide(
                        new BigNumber("123000", Locale.GERMAN),
                        new BigNumber("-456", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideSecondNegativeTenDigitIntegersTest() {
        assertEquals(
                "-8,0000000729000007",
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
                "5,3000340202177294",
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
                "269,7368421052631579",
                BasicMath.divide(
                        new BigNumber("-123000", Locale.GERMAN),
                        new BigNumber("-456", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideBothNegativeTenDigitIntegersTest() {
        assertEquals(
                "8,0000000729000007",
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
                "8,024390243902439",
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
                "0,2697368421052632",
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
                "0,000000008100000073710001",
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
                "0,00000002430000022113",
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
                "0,0004997501249375312",
                BasicMath.divide(
                        new BigNumber("0,5", Locale.GERMAN),
                        new BigNumber("1000,5", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    // Ein Ergebnis, das nicht endet, wird auf 16 Stellen gerundet (MathContext.DECIMAL64, also HALF_EVEN)
    @Test
    public void divideRoundsOneThirdTest() {
        assertEquals(
                "0,3333333333333333",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRoundsTwoThirdsUpTest() {
        assertEquals(
                "0,6666666666666667",
                BasicMath.divide(
                        new BigNumber("2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRoundsOneSixthTest() {
        assertEquals(
                "0,1666666666666667",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("6", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRoundsFiveSixthsTest() {
        assertEquals(
                "0,8333333333333333",
                BasicMath.divide(
                        new BigNumber("5", Locale.GERMAN),
                        new BigNumber("6", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRoundsOneSeventhTest() {
        assertEquals(
                "0,1428571428571429",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("7", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRoundsOneNinthTest() {
        assertEquals(
                "0,1111111111111111",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("9", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRoundsEightNinthsUpTest() {
        assertEquals(
                "0,8888888888888889",
                BasicMath.divide(
                        new BigNumber("8", Locale.GERMAN),
                        new BigNumber("9", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRoundsOneEleventhTest() {
        assertEquals(
                "0,09090909090909091",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("11", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRoundsOneThirteenthTest() {
        assertEquals(
                "0,07692307692307692",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("13", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRoundsWithIntegerPartTest() {
        assertEquals(
                "3,3333333333333333",
                BasicMath.divide(
                        new BigNumber("10", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRoundsApproximationOfPiTest() {
        assertEquals(
                "3,1428571428571429",
                BasicMath.divide(
                        new BigNumber("22", Locale.GERMAN),
                        new BigNumber("7", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRoundsNegativeTwoThirdsTest() {
        assertEquals(
                "-0,6666666666666667",
                BasicMath.divide(
                        new BigNumber("-2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRoundsNegativeDivisorTwoThirdsTest() {
        assertEquals(
                "-0,6666666666666667",
                BasicMath.divide(
                        new BigNumber("2", Locale.GERMAN),
                        new BigNumber("-3", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRoundsBothNegativeTwoThirdsTest() {
        assertEquals(
                "0,6666666666666667",
                BasicMath.divide(
                        new BigNumber("-2", Locale.GERMAN),
                        new BigNumber("-3", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideEndingResultWithElevenDecimalsIsNotRoundedTest() {
        assertEquals(
                "0,12345678999",
                BasicMath.divide(
                        new BigNumber("0,12345678999", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideEndingResultWithTrailingZeroInDividendIsNotRoundedTest() {
        assertEquals(
                "0,12345678901",
                BasicMath.divide(
                        new BigNumber("0,12345678901", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    // Ein Ergebnis, das endet, wird nicht gerundet, solange es in 16 Stellen passt
    @Test
    public void divideEndingResultWithTenDecimalPlacesTest() {
        assertEquals(
                "0,0009765625",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("1024", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideEndingResultWithElevenDecimalPlacesIsNotRoundedTest() {
        assertEquals(
                "0,00048828125",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("2048", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideVerySmallEndingResultTest() {
        assertEquals(
                "0,0000000001",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("10000000000", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideVerySmallResultIsNotZeroTest() {
        assertEquals(
                "0.00000000001",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("100000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideVerySmallResultWithNineIsNotZeroTest() {
        assertEquals(
                "0.00000000009",
                BasicMath.divide(
                        new BigNumber("9", Locale.GERMAN),
                        new BigNumber("100000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideVerySmallDividendByOneIsNotZeroTest() {
        assertEquals(
                "0.00000000001",
                BasicMath.divide(
                        new BigNumber("0,00000000001", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideVerySmallDecimalStaysAfterDivisionByOneTest() {
        assertEquals(
                "0,0000000001",
                BasicMath.divide(
                        new BigNumber("0,0000000001", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideDecimalsWithIntegerPartStayAfterDivisionByOneTest() {
        assertEquals(
                "12345,0000000001",
                BasicMath.divide(
                        new BigNumber("12345,0000000001", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    // Wiederholungen werden erkannt: die Rechnung bricht ab und rundet auf 16 Stellen (Standard)
    @Test
    public void divideRepeatingPeriodOfOneDigitTest() {
        assertEquals(
                "0,3333333333333333",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRepeatingPeriodOfTwoDigitsTest() {
        assertEquals(
                "0,4545454545454545",
                BasicMath.divide(
                        new BigNumber("5", Locale.GERMAN),
                        new BigNumber("11", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRepeatingPeriodOfThreeDigitsTest() {
        assertEquals(
                "0,001001001001001001",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("999", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRepeatingPeriodOfFourDigitsTest() {
        assertEquals(
                "0,009900990099009901",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("101", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRepeatingPeriodOfSixDigitsTest() {
        assertEquals(
                "0,2857142857142857",
                BasicMath.divide(
                        new BigNumber("2", Locale.GERMAN),
                        new BigNumber("7", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRepeatingPeriodOfNineDigitsTest() {
        assertEquals(
                "0,01234567901234568",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("81", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRepeatingPeriodOfSixteenDigitsTest() {
        assertEquals(
                "0,05882352941176471",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("17", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRepeatingPeriodLongerThanPrecisionTest() {
        assertEquals(
                "0,01030927835051546",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("97", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRepeatingPeriodWithPartBeforeRepetitionTest() {
        assertEquals(
                "0,2333333333333333",
                BasicMath.divide(
                        new BigNumber("7", Locale.GERMAN),
                        new BigNumber("30", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRepeatingPeriodAfterTwoDigitsBeforeRepetitionTest() {
        assertEquals(
                "0,08333333333333333",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("12", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRepeatingPeriodWithIntegerPartTest() {
        assertEquals(
                "14,2857142857142857",
                BasicMath.divide(
                        new BigNumber("100", Locale.GERMAN),
                        new BigNumber("7", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRepeatingPeriodWithLargeIntegerPartTest() {
        assertEquals(
                "17636684,1428571428571429",
                BasicMath.divide(
                        new BigNumber("123456789", Locale.GERMAN),
                        new BigNumber("7", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRepeatingPeriodWithDecimalDividendTest() {
        assertEquals(
                "0,07142857142857143",
                BasicMath.divide(
                        new BigNumber("0,5", Locale.GERMAN),
                        new BigNumber("7", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRepeatingPeriodWithDecimalDivisorTest() {
        assertEquals(
                "33,3333333333333333",
                BasicMath.divide(
                        new BigNumber("10", Locale.GERMAN),
                        new BigNumber("0,3", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRepeatingPeriodOfZerosAndOnesTest() {
        assertEquals(
                "0,0001020304050607081",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("9801", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRepeatingNegativeTest() {
        assertEquals(
                "-0,1428571428571429",
                BasicMath.divide(
                        new BigNumber("-1", Locale.GERMAN),
                        new BigNumber("7", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRepeatingNegativeDivisorTest() {
        assertEquals(
                "-0,1428571428571429",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("-7", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRepeatingBothNegativeTest() {
        assertEquals(
                "0,1428571428571429",
                BasicMath.divide(
                        new BigNumber("-1", Locale.GERMAN),
                        new BigNumber("-7", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRepeatingRoundsUpAtCarryTest() {
        assertEquals(
                "0,1111111111111111",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("9", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRepeatingVerySmallDividendTest() {
        assertEquals(
                "0,000000001428571428571429",
                BasicMath.divide(
                        new BigNumber("0,00000001", Locale.GERMAN),
                        new BigNumber("7", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    // MathContext: die Genauigkeit zählt die gültigen Ziffern, 1 / 3 mit unterschiedlicher Genauigkeit
    @Test
    public void divideOneThirdWithPrecision1Test() {
        assertEquals(
                "0,3",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(1, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideOneThirdWithPrecision2Test() {
        assertEquals(
                "0,33",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideOneThirdWithPrecision3Test() {
        assertEquals(
                "0,333",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(3, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideOneThirdWithPrecision5Test() {
        assertEquals(
                "0,33333",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(5, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideOneThirdWithPrecision10Test() {
        assertEquals(
                "0,3333333333",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(10, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideOneThirdWithPrecision16Test() {
        assertEquals(
                "0,3333333333333333",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(16, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideOneThirdWithPrecision20Test() {
        assertEquals(
                "0,33333333333333333333",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(20, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideOneThirdWithPrecision34Test() {
        assertEquals(
                "0,3333333333333333333333333333333333",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(34, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideOneThirdWithPrecision50Test() {
        assertEquals(
                "0,33333333333333333333333333333333333333333333333333",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(50, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideOneThirdWithPrecision100Test() {
        assertEquals(
                "0,3333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(100, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideOneThirdWithPrecision1000Test() {
        assertEquals(
                "0,3333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(1000, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideTwoThirdsWithPrecision1Test() {
        assertEquals(
                "0,7",
                BasicMath.divide(
                        new BigNumber("2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(1, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideTwoThirdsWithPrecision2Test() {
        assertEquals(
                "0,67",
                BasicMath.divide(
                        new BigNumber("2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideTwoThirdsWithPrecision3Test() {
        assertEquals(
                "0,667",
                BasicMath.divide(
                        new BigNumber("2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(3, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideTwoThirdsWithPrecision5Test() {
        assertEquals(
                "0,66667",
                BasicMath.divide(
                        new BigNumber("2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(5, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideTwoThirdsWithPrecision10Test() {
        assertEquals(
                "0,6666666667",
                BasicMath.divide(
                        new BigNumber("2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(10, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideTwoThirdsWithPrecision34Test() {
        assertEquals(
                "0,6666666666666666666666666666666667",
                BasicMath.divide(
                        new BigNumber("2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(34, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideTwoThirdsWithPrecision100Test() {
        assertEquals(
                "0,6666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666667",
                BasicMath.divide(
                        new BigNumber("2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(100, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideOneSeventhWithPrecision1Test() {
        assertEquals(
                "0,1",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("7", Locale.GERMAN),
                        new MathContext(1, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideOneSeventhWithPrecision3Test() {
        assertEquals(
                "0,143",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("7", Locale.GERMAN),
                        new MathContext(3, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideOneSeventhWithPrecision6Test() {
        assertEquals(
                "0,142857",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("7", Locale.GERMAN),
                        new MathContext(6, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideOneSeventhWithPrecision7Test() {
        assertEquals(
                "0,1428571",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("7", Locale.GERMAN),
                        new MathContext(7, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideOneSeventhWithPrecision8Test() {
        assertEquals(
                "0,14285714",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("7", Locale.GERMAN),
                        new MathContext(8, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideOneSeventhWithPrecision13Test() {
        assertEquals(
                "0,1428571428571",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("7", Locale.GERMAN),
                        new MathContext(13, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideOneSeventhWithPrecision34Test() {
        assertEquals(
                "0,1428571428571428571428571428571429",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("7", Locale.GERMAN),
                        new MathContext(34, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideOneSeventhWithPrecision100Test() {
        assertEquals(
                "0,1428571428571428571428571428571428571428571428571428571428571428571428571428571428571428571428571429",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("7", Locale.GERMAN),
                        new MathContext(100, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideOneSeventhWithPrecision200Test() {
        assertEquals(
                "0,14285714285714285714285714285714285714285714285714285714285714285714285714285714285714285714285714285714285714285714285714285714285714285714285714285714285714285714285714285714285714285714285714285714",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("7", Locale.GERMAN),
                        new MathContext(200, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideOneNinetySeventhWithPrecision1Test() {
        assertEquals(
                "0,01",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("97", Locale.GERMAN),
                        new MathContext(1, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideOneNinetySeventhWithPrecision50Test() {
        assertEquals(
                "0,01030927835051546391752577319587628865979381443299",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("97", Locale.GERMAN),
                        new MathContext(50, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideOneNinetySeventhWithPrecision96Test() {
        assertEquals(
                "0,010309278350515463917525773195876288659793814432989690721649484536082474226804123711340206185567",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("97", Locale.GERMAN),
                        new MathContext(96, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideOneNinetySeventhWithPrecision97Test() {
        assertEquals(
                "0,01030927835051546391752577319587628865979381443298969072164948453608247422680412371134020618556701",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("97", Locale.GERMAN),
                        new MathContext(97, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideOneNinetySeventhWithPrecision200Test() {
        assertEquals(
                "0,010309278350515463917525773195876288659793814432989690721649484536082474226804123711340206185567010309278350515463917525773195876288659793814432989690721649484536082474226804123711340206185567010309278",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("97", Locale.GERMAN),
                        new MathContext(200, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideOneNinetySeventhWithPrecision500Test() {
        assertEquals(
                "0,010309278350515463917525773195876288659793814432989690721649484536082474226804123711340206185567010309278350515463917525773195876288659793814432989690721649484536082474226804123711340206185567010309278350515463917525773195876288659793814432989690721649484536082474226804123711340206185567010309278350515463917525773195876288659793814432989690721649484536082474226804123711340206185567010309278350515463917525773195876288659793814432989690721649484536082474226804123711340206185567010309278350515463918",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("97", Locale.GERMAN),
                        new MathContext(500, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideEndingResultWithPrecision5Test() {
        assertEquals(
                "3,5",
                BasicMath.divide(
                        new BigNumber("7", Locale.GERMAN),
                        new BigNumber("2", Locale.GERMAN),
                        new MathContext(5, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideEndingResultWithPrecision34Test() {
        assertEquals(
                "3,5",
                BasicMath.divide(
                        new BigNumber("7", Locale.GERMAN),
                        new BigNumber("2", Locale.GERMAN),
                        new MathContext(34, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    // MathContext: vordefinierte Werte
    @Test
    public void divideWithDecimal32Test() {
        assertEquals(
                "0,3333333",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        MathContext.DECIMAL32
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideWithDecimal64Test() {
        assertEquals(
                "0,3333333333333333",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        MathContext.DECIMAL64
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideWithDecimal128Test() {
        assertEquals(
                "0,3333333333333333333333333333333333",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        MathContext.DECIMAL128
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideWithDecimal32RoundsUpTest() {
        assertEquals(
                "0,6666667",
                BasicMath.divide(
                        new BigNumber("2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        MathContext.DECIMAL32
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideWithoutMathContextIsSameAsDecimal64Test() {
        assertEquals(
                BasicMath.divide(
                        new BigNumber("22", Locale.GERMAN),
                        new BigNumber("7", Locale.GERMAN),
                        MathContext.DECIMAL64
                ).toString(),
                BasicMath.divide(
                        new BigNumber("22", Locale.GERMAN),
                        new BigNumber("7", Locale.GERMAN)
                ).toString()
        );
    }

    // Rundungsarten bei einem Ergebnis, das nicht endet (2 / 3 = 0,666... mit 3 Stellen)
    @Test
    public void divideUpTwoThirdsWithPrecision3Test() {
        assertEquals(
                "0,667",
                BasicMath.divide(
                        new BigNumber("2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(3, RoundingMode.UP)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideDownTwoThirdsWithPrecision3Test() {
        assertEquals(
                "0,666",
                BasicMath.divide(
                        new BigNumber("2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(3, RoundingMode.DOWN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideCeilingTwoThirdsWithPrecision3Test() {
        assertEquals(
                "0,667",
                BasicMath.divide(
                        new BigNumber("2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(3, RoundingMode.CEILING)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideFloorTwoThirdsWithPrecision3Test() {
        assertEquals(
                "0,666",
                BasicMath.divide(
                        new BigNumber("2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(3, RoundingMode.FLOOR)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfUpTwoThirdsWithPrecision3Test() {
        assertEquals(
                "0,667",
                BasicMath.divide(
                        new BigNumber("2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(3, RoundingMode.HALF_UP)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfDownTwoThirdsWithPrecision3Test() {
        assertEquals(
                "0,667",
                BasicMath.divide(
                        new BigNumber("2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(3, RoundingMode.HALF_DOWN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfEvenTwoThirdsWithPrecision3Test() {
        assertEquals(
                "0,667",
                BasicMath.divide(
                        new BigNumber("2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(3, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideUpNegativeTwoThirdsWithPrecision3Test() {
        assertEquals(
                "-0,667",
                BasicMath.divide(
                        new BigNumber("-2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(3, RoundingMode.UP)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideDownNegativeTwoThirdsWithPrecision3Test() {
        assertEquals(
                "-0,666",
                BasicMath.divide(
                        new BigNumber("-2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(3, RoundingMode.DOWN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideCeilingNegativeTwoThirdsWithPrecision3Test() {
        assertEquals(
                "-0,666",
                BasicMath.divide(
                        new BigNumber("-2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(3, RoundingMode.CEILING)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideFloorNegativeTwoThirdsWithPrecision3Test() {
        assertEquals(
                "-0,667",
                BasicMath.divide(
                        new BigNumber("-2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(3, RoundingMode.FLOOR)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfUpNegativeTwoThirdsWithPrecision3Test() {
        assertEquals(
                "-0,667",
                BasicMath.divide(
                        new BigNumber("-2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(3, RoundingMode.HALF_UP)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfDownNegativeTwoThirdsWithPrecision3Test() {
        assertEquals(
                "-0,667",
                BasicMath.divide(
                        new BigNumber("-2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(3, RoundingMode.HALF_DOWN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfEvenNegativeTwoThirdsWithPrecision3Test() {
        assertEquals(
                "-0,667",
                BasicMath.divide(
                        new BigNumber("-2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(3, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideUpOneThirdWithPrecision3Test() {
        assertEquals(
                "0,334",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(3, RoundingMode.UP)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideUpNegativeOneThirdWithPrecision3Test() {
        assertEquals(
                "-0,334",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("-3", Locale.GERMAN),
                        new MathContext(3, RoundingMode.UP)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideDownOneThirdWithPrecision3Test() {
        assertEquals(
                "0,333",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(3, RoundingMode.DOWN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideDownNegativeOneThirdWithPrecision3Test() {
        assertEquals(
                "-0,333",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("-3", Locale.GERMAN),
                        new MathContext(3, RoundingMode.DOWN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideCeilingOneThirdWithPrecision3Test() {
        assertEquals(
                "0,334",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(3, RoundingMode.CEILING)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideCeilingNegativeOneThirdWithPrecision3Test() {
        assertEquals(
                "-0,333",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("-3", Locale.GERMAN),
                        new MathContext(3, RoundingMode.CEILING)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideFloorOneThirdWithPrecision3Test() {
        assertEquals(
                "0,333",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(3, RoundingMode.FLOOR)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideFloorNegativeOneThirdWithPrecision3Test() {
        assertEquals(
                "-0,334",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("-3", Locale.GERMAN),
                        new MathContext(3, RoundingMode.FLOOR)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfUpOneThirdWithPrecision3Test() {
        assertEquals(
                "0,333",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(3, RoundingMode.HALF_UP)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfUpNegativeOneThirdWithPrecision3Test() {
        assertEquals(
                "-0,333",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("-3", Locale.GERMAN),
                        new MathContext(3, RoundingMode.HALF_UP)
                ).toString(Locale.GERMAN)
        );
    }

    // Rundungsarten bei genau der Hälfte (das Ergebnis endet, hat aber eine Stelle zu viel)
    @Test
    public void divideUpOneEighthWithPrecision2Test() {
        assertEquals(
                "0,13",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.UP)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideDownOneEighthWithPrecision2Test() {
        assertEquals(
                "0,12",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.DOWN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideCeilingOneEighthWithPrecision2Test() {
        assertEquals(
                "0,13",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.CEILING)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideFloorOneEighthWithPrecision2Test() {
        assertEquals(
                "0,12",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.FLOOR)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfUpOneEighthWithPrecision2Test() {
        assertEquals(
                "0,13",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_UP)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfDownOneEighthWithPrecision2Test() {
        assertEquals(
                "0,12",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_DOWN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfEvenOneEighthWithPrecision2Test() {
        assertEquals(
                "0,12",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideUpThreeEighthsWithPrecision2Test() {
        assertEquals(
                "0,38",
                BasicMath.divide(
                        new BigNumber("3", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.UP)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideDownThreeEighthsWithPrecision2Test() {
        assertEquals(
                "0,37",
                BasicMath.divide(
                        new BigNumber("3", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.DOWN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideCeilingThreeEighthsWithPrecision2Test() {
        assertEquals(
                "0,38",
                BasicMath.divide(
                        new BigNumber("3", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.CEILING)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideFloorThreeEighthsWithPrecision2Test() {
        assertEquals(
                "0,37",
                BasicMath.divide(
                        new BigNumber("3", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.FLOOR)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfUpThreeEighthsWithPrecision2Test() {
        assertEquals(
                "0,38",
                BasicMath.divide(
                        new BigNumber("3", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_UP)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfDownThreeEighthsWithPrecision2Test() {
        assertEquals(
                "0,37",
                BasicMath.divide(
                        new BigNumber("3", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_DOWN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfEvenThreeEighthsWithPrecision2Test() {
        assertEquals(
                "0,38",
                BasicMath.divide(
                        new BigNumber("3", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideUpNegativeOneEighthWithPrecision2Test() {
        assertEquals(
                "-0,13",
                BasicMath.divide(
                        new BigNumber("-1", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.UP)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideDownNegativeOneEighthWithPrecision2Test() {
        assertEquals(
                "-0,12",
                BasicMath.divide(
                        new BigNumber("-1", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.DOWN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideCeilingNegativeOneEighthWithPrecision2Test() {
        assertEquals(
                "-0,12",
                BasicMath.divide(
                        new BigNumber("-1", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.CEILING)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideFloorNegativeOneEighthWithPrecision2Test() {
        assertEquals(
                "-0,13",
                BasicMath.divide(
                        new BigNumber("-1", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.FLOOR)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfUpNegativeOneEighthWithPrecision2Test() {
        assertEquals(
                "-0,13",
                BasicMath.divide(
                        new BigNumber("-1", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_UP)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfDownNegativeOneEighthWithPrecision2Test() {
        assertEquals(
                "-0,12",
                BasicMath.divide(
                        new BigNumber("-1", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_DOWN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfEvenNegativeOneEighthWithPrecision2Test() {
        assertEquals(
                "-0,12",
                BasicMath.divide(
                        new BigNumber("-1", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideUpNegativeThreeEighthsWithPrecision2Test() {
        assertEquals(
                "-0,38",
                BasicMath.divide(
                        new BigNumber("-3", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.UP)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideDownNegativeThreeEighthsWithPrecision2Test() {
        assertEquals(
                "-0,37",
                BasicMath.divide(
                        new BigNumber("-3", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.DOWN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideCeilingNegativeThreeEighthsWithPrecision2Test() {
        assertEquals(
                "-0,37",
                BasicMath.divide(
                        new BigNumber("-3", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.CEILING)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideFloorNegativeThreeEighthsWithPrecision2Test() {
        assertEquals(
                "-0,38",
                BasicMath.divide(
                        new BigNumber("-3", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.FLOOR)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfUpNegativeThreeEighthsWithPrecision2Test() {
        assertEquals(
                "-0,38",
                BasicMath.divide(
                        new BigNumber("-3", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_UP)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfDownNegativeThreeEighthsWithPrecision2Test() {
        assertEquals(
                "-0,37",
                BasicMath.divide(
                        new BigNumber("-3", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_DOWN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfEvenNegativeThreeEighthsWithPrecision2Test() {
        assertEquals(
                "-0,38",
                BasicMath.divide(
                        new BigNumber("-3", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfUpFiveQuartersWithPrecision1Test() {
        assertEquals(
                "1.3",
                BasicMath.divide(
                        new BigNumber("5", Locale.GERMAN),
                        new BigNumber("4", Locale.GERMAN),
                        new MathContext(1, RoundingMode.HALF_UP)
                ).toString()
        );
    }

    @Test
    public void divideHalfDownFiveQuartersWithPrecision1Test() {
        assertEquals(
                "1.2",
                BasicMath.divide(
                        new BigNumber("5", Locale.GERMAN),
                        new BigNumber("4", Locale.GERMAN),
                        new MathContext(1, RoundingMode.HALF_DOWN)
                ).toString()
        );
    }

    @Test
    public void divideHalfEvenFiveQuartersWithPrecision1Test() {
        assertEquals(
                "1.2",
                BasicMath.divide(
                        new BigNumber("5", Locale.GERMAN),
                        new BigNumber("4", Locale.GERMAN),
                        new MathContext(1, RoundingMode.HALF_EVEN)
                ).toString()
        );
    }

    @Test
    public void divideHalfUpSevenQuartersWithPrecision1Test() {
        assertEquals(
                "1.8",
                BasicMath.divide(
                        new BigNumber("7", Locale.GERMAN),
                        new BigNumber("4", Locale.GERMAN),
                        new MathContext(1, RoundingMode.HALF_UP)
                ).toString()
        );
    }

    @Test
    public void divideHalfDownSevenQuartersWithPrecision1Test() {
        assertEquals(
                "1.7",
                BasicMath.divide(
                        new BigNumber("7", Locale.GERMAN),
                        new BigNumber("4", Locale.GERMAN),
                        new MathContext(1, RoundingMode.HALF_DOWN)
                ).toString()
        );
    }

    @Test
    public void divideHalfEvenSevenQuartersWithPrecision1Test() {
        assertEquals(
                "1.8",
                BasicMath.divide(
                        new BigNumber("7", Locale.GERMAN),
                        new BigNumber("4", Locale.GERMAN),
                        new MathContext(1, RoundingMode.HALF_EVEN)
                ).toString()
        );
    }

    @Test
    public void divideHalfUpNegativeFiveQuartersWithPrecision1Test() {
        assertEquals(
                "-1.3",
                BasicMath.divide(
                        new BigNumber("-5", Locale.GERMAN),
                        new BigNumber("4", Locale.GERMAN),
                        new MathContext(1, RoundingMode.HALF_UP)
                ).toString()
        );
    }

    @Test
    public void divideHalfDownNegativeFiveQuartersWithPrecision1Test() {
        assertEquals(
                "-1.2",
                BasicMath.divide(
                        new BigNumber("-5", Locale.GERMAN),
                        new BigNumber("4", Locale.GERMAN),
                        new MathContext(1, RoundingMode.HALF_DOWN)
                ).toString()
        );
    }

    @Test
    public void divideHalfEvenNegativeFiveQuartersWithPrecision1Test() {
        assertEquals(
                "-1.2",
                BasicMath.divide(
                        new BigNumber("-5", Locale.GERMAN),
                        new BigNumber("4", Locale.GERMAN),
                        new MathContext(1, RoundingMode.HALF_EVEN)
                ).toString()
        );
    }

    @Test
    public void divideHalfUpNegativeSevenQuartersWithPrecision1Test() {
        assertEquals(
                "-1.8",
                BasicMath.divide(
                        new BigNumber("-7", Locale.GERMAN),
                        new BigNumber("4", Locale.GERMAN),
                        new MathContext(1, RoundingMode.HALF_UP)
                ).toString()
        );
    }

    @Test
    public void divideHalfDownNegativeSevenQuartersWithPrecision1Test() {
        assertEquals(
                "-1.7",
                BasicMath.divide(
                        new BigNumber("-7", Locale.GERMAN),
                        new BigNumber("4", Locale.GERMAN),
                        new MathContext(1, RoundingMode.HALF_DOWN)
                ).toString()
        );
    }

    @Test
    public void divideHalfEvenNegativeSevenQuartersWithPrecision1Test() {
        assertEquals(
                "-1.8",
                BasicMath.divide(
                        new BigNumber("-7", Locale.GERMAN),
                        new BigNumber("4", Locale.GERMAN),
                        new MathContext(1, RoundingMode.HALF_EVEN)
                ).toString()
        );
    }

    // Rundungsarten knapp unter und knapp über der Hälfte
    @Test
    public void divideHalfEvenJustBelowHalfTest() {
        assertEquals(
                "0,12",
                BasicMath.divide(
                        new BigNumber("0,1249", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfEvenJustAboveHalfTest() {
        assertEquals(
                "0,13",
                BasicMath.divide(
                        new BigNumber("0,1251", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfUpJustBelowHalfTest() {
        assertEquals(
                "0,12",
                BasicMath.divide(
                        new BigNumber("0,1249", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_UP)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfUpJustAboveHalfTest() {
        assertEquals(
                "0,13",
                BasicMath.divide(
                        new BigNumber("0,1251", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_UP)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfDownJustAboveHalfTest() {
        assertEquals(
                "0,13",
                BasicMath.divide(
                        new BigNumber("0,1251", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_DOWN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfDownJustBelowHalfTest() {
        assertEquals(
                "0,12",
                BasicMath.divide(
                        new BigNumber("0,1249", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_DOWN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfEvenAtHalfWithEvenLastDigitTest() {
        assertEquals(
                "0,12",
                BasicMath.divide(
                        new BigNumber("0,125", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfEvenAtHalfWithOddLastDigitTest() {
        assertEquals(
                "0,14",
                BasicMath.divide(
                        new BigNumber("0,135", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfEvenAtHalfWithZeroAsLastDigitTest() {
        assertEquals(
                "0,1",
                BasicMath.divide(
                        new BigNumber("0,105", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideHalfEvenAtHalfWithNineAsLastDigitTest() {
        assertEquals(
                "0,2",
                BasicMath.divide(
                        new BigNumber("0,195", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    // UNNECESSARY: erlaubt, solange nichts gerundet werden muss
    @Test
    public void divideUnnecessaryWithEndingResultTest() {
        assertEquals(
                "3,5",
                BasicMath.divide(
                        new BigNumber("7", Locale.GERMAN),
                        new BigNumber("2", Locale.GERMAN),
                        new MathContext(5, RoundingMode.UNNECESSARY)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideUnnecessaryWithEndingResultOfExactlyPrecisionTest() {
        assertEquals(
                "0,125",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(3, RoundingMode.UNNECESSARY)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideUnnecessaryWithIntegerResultTest() {
        assertEquals(
                "12",
                BasicMath.divide(
                        new BigNumber("144", Locale.GERMAN),
                        new BigNumber("12", Locale.GERMAN),
                        new MathContext(2, RoundingMode.UNNECESSARY)
                ).toString()
        );
    }

    @Test
    public void divideUnnecessaryWithIntegerResultLongerThanPrecisionTest() {
        assertEquals(
                "1000000000000000000000000000000",
                BasicMath.divide(
                        new BigNumber("1000000000000000000000000000000", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN),
                        new MathContext(3, RoundingMode.UNNECESSARY)
                ).toString()
        );
    }

    @Test
    public void divideUnnecessaryWithRepeatingResultThrowsTest() {
        assertThrows(
                RoundingIsNecessaryException.class,
                () -> BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(10, RoundingMode.UNNECESSARY)
                )
        );
    }

    @Test
    public void divideUnnecessaryWithEndingResultTooLongThrowsTest() {
        assertThrows(
                RoundingIsNecessaryException.class,
                () -> BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(2, RoundingMode.UNNECESSARY)
                )
        );
    }

    @Test
    public void divideUnnecessaryWithHalfThrowsTest() {
        assertThrows(
                RoundingIsNecessaryException.class,
                () -> BasicMath.divide(
                        new BigNumber("5", Locale.GERMAN),
                        new BigNumber("4", Locale.GERMAN),
                        new MathContext(1, RoundingMode.UNNECESSARY)
                )
        );
    }

    @Test
    public void divideUnnecessaryWithNegativeRepeatingResultThrowsTest() {
        assertThrows(
                RoundingIsNecessaryException.class,
                () -> BasicMath.divide(
                        new BigNumber("-2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(5, RoundingMode.UNNECESSARY)
                )
        );
    }

    @Test
    public void divideUnnecessaryWithRepeatingResultAndHighPrecisionThrowsTest() {
        assertThrows(
                RoundingIsNecessaryException.class,
                () -> BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("7", Locale.GERMAN),
                        new MathContext(500, RoundingMode.UNNECESSARY)
                )
        );
    }

    // Die Genauigkeit zählt nur die Nachkommastellen, die Stellen vor dem Dezimaltrenner werden nie gerundet
    @Test
    public void divideLargeIntegerPartKeepsAllDecimalPlacesTest() {
        assertEquals(
                "333333333333333333333333333333333333333.33333",
                BasicMath.divide(
                        new BigNumber("1000000000000000000000000000000000000000", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(5, RoundingMode.HALF_EVEN)
                ).toString()
        );
    }

    @Test
    public void divideLargeIntegerPartRoundsDecimalPlacesUpTest() {
        assertEquals(
                "666666666666666666666666666666666666666.66667",
                BasicMath.divide(
                        new BigNumber("2000000000000000000000000000000000000000", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(5, RoundingMode.HALF_EVEN)
                ).toString()
        );
    }

    @Test
    public void divideIntegerPartWithThreeDigitsAndPrecision2Test() {
        assertEquals(
                "33.33",
                BasicMath.divide(
                        new BigNumber("100", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_EVEN)
                ).toString()
        );
    }

    @Test
    public void divideIntegerPartWithThreeDigitsAndPrecision2RoundsUpTest() {
        assertEquals(
                "66.67",
                BasicMath.divide(
                        new BigNumber("200", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_EVEN)
                ).toString()
        );
    }

    @Test
    public void divideIntegerPartWithFourDigitsAndPrecision2Test() {
        assertEquals(
                "333.33",
                BasicMath.divide(
                        new BigNumber("1000", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_EVEN)
                ).toString()
        );
    }

    @Test
    public void divideIntegerPartWithFourDigitsAndPrecision2RoundsUpTest() {
        assertEquals(
                "666.67",
                BasicMath.divide(
                        new BigNumber("2000", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_EVEN)
                ).toString()
        );
    }

    @Test
    public void divideIntegerPartWithThreeDigitsAndPrecision5Test() {
        assertEquals(
                "33,33333",
                BasicMath.divide(
                        new BigNumber("100", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(5, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideIntegerPartWithPrecision1Test() {
        assertEquals(
                "33.3",
                BasicMath.divide(
                        new BigNumber("100", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(1, RoundingMode.HALF_EVEN)
                ).toString()
        );
    }

    @Test
    public void divideIntegerPartWithPrecision1RoundsUpTest() {
        assertEquals(
                "66.7",
                BasicMath.divide(
                        new BigNumber("200", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(1, RoundingMode.UP)
                ).toString()
        );
    }

    @Test
    public void divideIntegerPartWithPrecision1AndDownTest() {
        assertEquals(
                "66.6",
                BasicMath.divide(
                        new BigNumber("200", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(1, RoundingMode.DOWN)
                ).toString()
        );
    }

    @Test
    public void divideEndingResultWithIntegerPartRoundsHalfUpTest() {
        assertEquals(
                "123456789.6",
                BasicMath.divide(
                        new BigNumber("123456789,55", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN),
                        new MathContext(1, RoundingMode.HALF_UP)
                ).toString()
        );
    }

    @Test
    public void divideEndingResultWithIntegerPartRoundsHalfDownTest() {
        assertEquals(
                "123456789.5",
                BasicMath.divide(
                        new BigNumber("123456789,55", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN),
                        new MathContext(1, RoundingMode.HALF_DOWN)
                ).toString()
        );
    }

    @Test
    public void divideNegativeLargeIntegerPartRoundsAwayFromZeroTest() {
        assertEquals(
                "-666666666666666666666666666666666666666.66667",
                BasicMath.divide(
                        new BigNumber("-2000000000000000000000000000000000000000", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(5, RoundingMode.HALF_EVEN)
                ).toString()
        );
    }

    @Test
    public void divideNegativeLargeIntegerPartWithCeilingTest() {
        assertEquals(
                "-666666666666666666666666666666666666666.66666",
                BasicMath.divide(
                        new BigNumber("-2000000000000000000000000000000000000000", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(5, RoundingMode.CEILING)
                ).toString()
        );
    }

    // Übertrag beim Aufrunden
    @Test
    public void divideRoundingCarryIntoIntegerPartTest() {
        assertEquals(
                "1",
                BasicMath.divide(
                        new BigNumber("0,9999", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_UP)
                ).toString()
        );
    }

    @Test
    public void divideRoundingCarryIntoNewDigitTest() {
        assertEquals(
                "10",
                BasicMath.divide(
                        new BigNumber("9,99", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN),
                        new MathContext(1, RoundingMode.HALF_UP)
                ).toString()
        );
    }

    @Test
    public void divideRoundingCarryOverManyNinesTest() {
        assertEquals(
                "100",
                BasicMath.divide(
                        new BigNumber("99,9999", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN),
                        new MathContext(3, RoundingMode.HALF_UP)
                ).toString()
        );
    }

    @Test
    public void divideRoundingCarryOfRepeatingResultTest() {
        assertEquals(
                "0,6",
                BasicMath.divide(
                        new BigNumber("5", Locale.GERMAN),
                        new BigNumber("9", Locale.GERMAN),
                        new MathContext(1, RoundingMode.HALF_UP)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideRoundingCarryOfRepeatingResultWithManyNinesTest() {
        assertEquals(
                "9",
                BasicMath.divide(
                        new BigNumber("8,9999999999", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN),
                        new MathContext(5, RoundingMode.HALF_UP)
                ).toString()
        );
    }

    @Test
    public void divideRoundingCarryWithCeilingTest() {
        assertEquals(
                "1",
                BasicMath.divide(
                        new BigNumber("0,99999", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN),
                        new MathContext(3, RoundingMode.CEILING)
                ).toString()
        );
    }

    @Test
    public void divideRoundingCarryWithUpTest() {
        assertEquals(
                "1",
                BasicMath.divide(
                        new BigNumber("0,99991", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN),
                        new MathContext(3, RoundingMode.UP)
                ).toString()
        );
    }

    @Test
    public void divideRoundingCarryNegativeWithFloorTest() {
        assertEquals(
                "-1",
                BasicMath.divide(
                        new BigNumber("-0,99999", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN),
                        new MathContext(3, RoundingMode.FLOOR)
                ).toString()
        );
    }

    @Test
    public void divideRoundingCarryOfRepeatingResultNineTest() {
        assertEquals(
                "0,2",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("9", Locale.GERMAN),
                        new MathContext(1, RoundingMode.UP)
                ).toString(Locale.GERMAN)
        );
    }

    // Zahlen unter 1: die Nullen direkt nach dem Komma zählen nicht zur Genauigkeit
    @Test
    public void divideLeadingZerosDoNotCountForPrecisionTest() {
        assertEquals(
                "0,00333",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("300", Locale.GERMAN),
                        new MathContext(3, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideLeadingZerosDoNotCountForPrecision5Test() {
        assertEquals(
                "0,00000066667",
                BasicMath.divide(
                        new BigNumber("2", Locale.GERMAN),
                        new BigNumber("3000000", Locale.GERMAN),
                        new MathContext(5, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideLeadingZerosDoNotCountForPrecision1Test() {
        assertEquals(
                "0,003",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("300", Locale.GERMAN),
                        new MathContext(1, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideLeadingZerosDoNotCountForPrecision1RoundsUpTest() {
        assertEquals(
                "0,0007",
                BasicMath.divide(
                        new BigNumber("2", Locale.GERMAN),
                        new BigNumber("3000", Locale.GERMAN),
                        new MathContext(1, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideLeadingZerosDoNotCountForPrecision16Test() {
        assertEquals(
                "0,0000000003333333333333333",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("3000000000", Locale.GERMAN),
                        new MathContext(16, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideLeadingZerosAfterRoundingCarryTest() {
        assertEquals(
                "0,01",
                BasicMath.divide(
                        new BigNumber("0,0099999", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_UP)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideManyLeadingZerosWithRepeatingResultTest() {
        assertEquals(
                "0,000000000000000000003333",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("300000000000000000000", Locale.GERMAN),
                        new MathContext(4, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideManyLeadingZerosWithRepeatingResultUpTest() {
        assertEquals(
                "0,000000000000000000003334",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("300000000000000000000", Locale.GERMAN),
                        new MathContext(4, RoundingMode.UP)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideLeadingZerosOfEndingResultAreNotRoundedTest() {
        assertEquals(
                "0,00000000000000000001",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("100000000000000000000", Locale.GERMAN),
                        new MathContext(2, RoundingMode.HALF_EVEN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideLeadingZerosWithNegativeRepeatingResultTest() {
        assertEquals(
                "-0,00334",
                BasicMath.divide(
                        new BigNumber("-1", Locale.GERMAN),
                        new BigNumber("300", Locale.GERMAN),
                        new MathContext(3, RoundingMode.FLOOR)
                ).toString(Locale.GERMAN)
        );
    }

    // Genauigkeit 0 (MathContext.UNLIMITED): es wird nie gerundet
    @Test
    public void divideUnlimitedWithEndingResultTest() {
        assertEquals(
                "3,5",
                BasicMath.divide(
                        new BigNumber("7", Locale.GERMAN),
                        new BigNumber("2", Locale.GERMAN),
                        new MathContext(0, RoundingMode.HALF_UP)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideUnlimitedWithLongEndingResultTest() {
        assertEquals(
                "0,0009765625",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("1024", Locale.GERMAN),
                        new MathContext(0, RoundingMode.HALF_UP)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideUnlimitedWithVeryLongEndingResultTest() {
        assertEquals(
                "0,000000000000000000867361737988403547205962240695953369140625",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("1152921504606846976", Locale.GERMAN),
                        new MathContext(0, RoundingMode.HALF_UP)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideUnlimitedWithPowerOfFiveTest() {
        assertEquals(
                "0,0000000000000000033554432",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("298023223876953125", Locale.GERMAN),
                        new MathContext(0, RoundingMode.HALF_UP)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideUnlimitedWithPowerOfTenTest() {
        assertEquals(
                "0,000000000000000000000000000001",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("1000000000000000000000000000000", Locale.GERMAN),
                        new MathContext(0, RoundingMode.HALF_UP)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideUnlimitedWithIntegerResultTest() {
        assertEquals(
                "250000000000000000000000000000000000000",
                BasicMath.divide(
                        new BigNumber("1000000000000000000000000000000000000000", Locale.GERMAN),
                        new BigNumber("4", Locale.GERMAN),
                        new MathContext(0, RoundingMode.HALF_UP)
                ).toString()
        );
    }

    @Test
    public void divideUnlimitedWithNegativeEndingResultTest() {
        assertEquals(
                "-0,125",
                BasicMath.divide(
                        new BigNumber("-1", Locale.GERMAN),
                        new BigNumber("8", Locale.GERMAN),
                        new MathContext(0, RoundingMode.HALF_UP)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideUnlimitedWithEndingResultOfDecimalsTest() {
        assertEquals(
                "0,125",
                BasicMath.divide(
                        new BigNumber("0,0001", Locale.GERMAN),
                        new BigNumber("0,0008", Locale.GERMAN),
                        new MathContext(0, RoundingMode.HALF_UP)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideUnlimitedWithTwoAndFiveInDivisorTest() {
        assertEquals(
                "0,025",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("40", Locale.GERMAN),
                        new MathContext(0, RoundingMode.HALF_UP)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideUnlimitedWithRepeatingResultThrowsTest() {
        assertThrows(
                ArithmeticException.class,
                () -> BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(0, RoundingMode.HALF_UP)
                )
        );
    }

    @Test
    public void divideUnlimitedWithRepeatingResultOfSevenThrowsTest() {
        assertThrows(
                ArithmeticException.class,
                () -> BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("7", Locale.GERMAN),
                        new MathContext(0, RoundingMode.HALF_UP)
                )
        );
    }

    @Test
    public void divideUnlimitedWithRepeatingResultAfterEndingPartThrowsTest() {
        assertThrows(
                ArithmeticException.class,
                () -> BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("12", Locale.GERMAN),
                        new MathContext(0, RoundingMode.HALF_UP)
                )
        );
    }

    @Test
    public void divideUnlimitedWithNegativeRepeatingResultThrowsTest() {
        assertThrows(
                ArithmeticException.class,
                () -> BasicMath.divide(
                        new BigNumber("-2", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(0, RoundingMode.HALF_UP)
                )
        );
    }

    @Test
    public void divideUnlimitedWithLongPeriodThrowsTest() {
        assertThrows(
                ArithmeticException.class,
                () -> BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("97", Locale.GERMAN),
                        new MathContext(0, RoundingMode.HALF_UP)
                )
        );
    }

    @Test
    public void divideUnlimitedWithVeryLongPeriodThrowsTest() {
        assertThrows(
                ArithmeticException.class,
                () -> BasicMath.divide(
                        new BigNumber("5559272386640", Locale.GERMAN),
                        new BigNumber("20933860,473203", Locale.GERMAN),
                        new MathContext(0, RoundingMode.HALF_UP)
                )
        );
    }

    @Test
    public void divideUnlimitedWithTwoAndFiveAndThreeInDivisorThrowsTest() {
        assertThrows(
                ArithmeticException.class,
                () -> BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("30", Locale.GERMAN),
                        new MathContext(0, RoundingMode.HALF_UP)
                )
        );
    }

    @Test
    public void divideUnlimitedWithVeryLongPeriodIsFastTest() {
        assertTimeoutPreemptively(
                Duration.ofSeconds(5),
                () -> assertThrows(
                        ArithmeticException.class,
                        () -> BasicMath.divide(
                                new BigNumber("5559272386640", Locale.GERMAN),
                                new BigNumber("20933860,473203", Locale.GERMAN),
                                MathContext.UNLIMITED
                        )
                )
        );
    }

    @Test
    public void divideUnlimitedWithRepeatingResultMentionsDividendTest() {
        final ArithmeticException exception = assertThrows(
                ArithmeticException.class,
                () -> BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        MathContext.UNLIMITED
                )
        );

        assertTrue(exception.getMessage().contains("1 / 3"));
    }

    // Hohe Genauigkeit: die Wiederholung wird übernommen, statt jede Stelle zu berechnen
    @Test
    public void divideHighPrecisionOfRepeatingResultIsFastTest() {
        assertTimeoutPreemptively(
                Duration.ofSeconds(5),
                () -> BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("7", Locale.GERMAN),
                        new MathContext(5000, RoundingMode.HALF_EVEN)
                )
        );
    }

    @Test
    public void divideHighPrecisionOfRepeatingResultHasAllDigitsTest() {
        final String result = BasicMath.divide(
                new BigNumber("1", Locale.GERMAN),
                new BigNumber("7", Locale.GERMAN),
                new MathContext(5000, RoundingMode.HALF_EVEN)
        ).toString();

        assertEquals(5002, result.length());
        assertTrue(result.startsWith("0.142857142857142857"));
        assertTrue(result.endsWith("285714285714"));
    }

    @Test
    public void divideHighPrecisionOfRepeatingResultRoundsLastDigitTest() {
        final String result = BasicMath.divide(
                new BigNumber("2", Locale.GERMAN),
                new BigNumber("3", Locale.GERMAN),
                new MathContext(5000, RoundingMode.HALF_EVEN)
        ).toString();

        assertTrue(result.startsWith("0.6666"));
        assertTrue(result.endsWith("667"));
    }

    @Test
    public void divideHighPrecisionOfRepeatingResultDownKeepsLastDigitTest() {
        final String result = BasicMath.divide(
                new BigNumber("2", Locale.GERMAN),
                new BigNumber("3", Locale.GERMAN),
                new MathContext(5000, RoundingMode.DOWN)
        ).toString();

        assertTrue(result.endsWith("666"));
    }

    // Der MathContext darf nicht null sein, und die Division durch 0 wird davor geprüft
    @Test
    public void divideWithNullMathContextThrowsTest() {
        assertThrows(
                NullPointerException.class,
                () -> BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        null
                )
        );
    }

    @Test
    public void divideByZeroWithMathContextThrowsTest() {
        assertThrows(
                DivisionByZeroException.class,
                () -> BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("0", Locale.GERMAN),
                        MathContext.UNLIMITED
                )
        );
    }

    @Test
    public void divideByNegatedZeroWithMathContextThrowsTest() {
        assertThrows(
                DivisionByZeroException.class,
                () -> BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("0", Locale.GERMAN).negate(),
                        new MathContext(5, RoundingMode.UNNECESSARY)
                )
        );
    }

    @Test
    public void divideZeroDividendWithUnnecessaryReturnsZeroTest() {
        assertEquals(
                "0",
                BasicMath.divide(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        new MathContext(5, RoundingMode.UNNECESSARY)
                ).toString()
        );
    }

    @Test
    public void divideZeroDividendWithUnlimitedReturnsZeroTest() {
        assertEquals(
                "0",
                BasicMath.divide(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN),
                        MathContext.UNLIMITED
                ).toString()
        );
    }

    // Aufruf über BigNumber mit MathContext
    @Test
    public void divideThroughBigNumberMethodWithMathContextTest() {
        assertEquals(
                "0,667",
                new BigNumber("2", Locale.GERMAN)
                        .divide(new BigNumber("3", Locale.GERMAN), new MathContext(3, RoundingMode.HALF_UP))
                        .toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideThroughBigNumberMethodWithMathContextRoundsDownTest() {
        assertEquals(
                "0,666",
                new BigNumber("2", Locale.GERMAN)
                        .divide(new BigNumber("3", Locale.GERMAN), new MathContext(3, RoundingMode.DOWN))
                        .toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideThroughBigNumberMethodWithMathContextByZeroThrowsTest() {
        assertThrows(
                DivisionByZeroException.class,
                () -> new BigNumber("2", Locale.GERMAN).divide(new BigNumber("0", Locale.GERMAN), MathContext.DECIMAL64)
        );
    }

    // Eingabewerte dürfen auch mit MathContext nicht verändert werden
    @Test
    public void divideWithMathContextDoesNotChangeDividendTest() {
        final BigNumber dividend = new BigNumber("-7,5", Locale.GERMAN);
        final BigNumber divisor = new BigNumber("0,3", Locale.GERMAN);

        BasicMath.divide(dividend, divisor, new MathContext(3, RoundingMode.UP));

        assertEquals("-7,5", dividend.toString(Locale.GERMAN));
    }

    @Test
    public void divideWithMathContextDoesNotChangeDivisorTest() {
        final BigNumber dividend = new BigNumber("-7,5", Locale.GERMAN);
        final BigNumber divisor = new BigNumber("0,3", Locale.GERMAN);

        BasicMath.divide(dividend, divisor, new MathContext(3, RoundingMode.UP));

        assertEquals("0,3", divisor.toString(Locale.GERMAN));
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

    // Ein sehr kleines Ergebnis wird nicht zu 0 und behält sein Vorzeichen
    @Test
    public void divideNegativeVerySmallResultTest() {
        assertEquals(
                "-0.00000000001",
                BasicMath.divide(
                        new BigNumber("-1", Locale.GERMAN),
                        new BigNumber("100000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideNegativeDivisorVerySmallResultTest() {
        assertEquals(
                "-0.00000000001",
                BasicMath.divide(
                        new BigNumber("1", Locale.GERMAN),
                        new BigNumber("-100000000000", Locale.GERMAN)
                ).toString()
        );
    }

    @Test
    public void divideNegativeVerySmallResultIsNegativeTest() {
        assertTrue(
                BasicMath.divide(
                        new BigNumber("-1", Locale.GERMAN),
                        new BigNumber("100000000000", Locale.GERMAN)
                ).isNegative()
        );
    }

    @Test
    public void divideNegativeDivisorVerySmallResultIsNegativeTest() {
        assertTrue(
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
                "8,0000000729000007",
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
                "333333333333333333333333333333333333333,3333333333333333",
                BasicMath.divide(
                        new BigNumber("1000000000000000000000000000000000000000", Locale.GERMAN),
                        new BigNumber("3", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideVeryLargeByVeryLargeWithDecimalResultTest() {
        assertEquals(
                "1,4285714285714286",
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
                "0.0000000000000000001111111111111111",
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
                "0,3333333333333333",
                BasicMath.divide(
                        new BigNumber("0,00000001", Locale.GERMAN),
                        new BigNumber("0,00000003", Locale.GERMAN)
                ).toString(Locale.GERMAN)
        );
    }

    @Test
    public void divideOneByVeryLargeIsZeroTest() {
        assertEquals(
                "0.000000000000000000000000000001",
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
    public void divideRoundedResultMultipliedDoesNotReachOriginalValueTest() {
        assertEquals(
                "0,9999999999999999",
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
                "1,9990004997501249",
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
                "0.2857142857142857",
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
