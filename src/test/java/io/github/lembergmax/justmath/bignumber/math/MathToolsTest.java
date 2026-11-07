package io.github.lembergmax.justmath.bignumber.math;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MathToolsTest {

    @Test
    public void makeNumbersSameLengthTest() {
        assertEquals(
                "123,12300",
                MathTools.makeNumbersSameLength(
                        new BigNumber("123,123", Locale.GERMAN),
                        new BigNumber("123,12345", Locale.GERMAN),
                        Locale.GERMAN
                )
        );
    }

    @Test
    public void signForAdditionResultNegativeTest() {
        assertEquals("-", MathTools.signForAdditionResult(true));
    }

    @Test
    public void signForAdditionResultPositiveTest() {
        assertEquals("+", MathTools.signForAdditionResult(false));
    }

    @Test
    public void sortBySizeIgnoreSignsFirstAddendLargerTest() {
        final BigNumber[] sorted = MathTools.sortBySizeIgnoreSigns(
                new BigNumber("10", Locale.GERMAN),
                new BigNumber("5", Locale.GERMAN)
        );
        assertEquals("10", sorted[0].toString());
        assertEquals("5", sorted[1].toString());
    }

    @Test
    public void sortBySizeIgnoreSignsSecondAddendLargerTest() {
        final BigNumber[] sorted = MathTools.sortBySizeIgnoreSigns(
                new BigNumber("5", Locale.GERMAN),
                new BigNumber("10", Locale.GERMAN)
        );
        assertEquals("10", sorted[0].toString());
        assertEquals("5", sorted[1].toString());
    }

    @Test
    public void sortBySizeIgnoreSignsIgnoresSignTest() {
        final BigNumber[] sorted = MathTools.sortBySizeIgnoreSigns(
                new BigNumber("-10", Locale.GERMAN),
                new BigNumber("5", Locale.GERMAN)
        );
        assertEquals("-10", sorted[0].toString());
        assertEquals("5", sorted[1].toString());
    }

    @Test
    public void sortBySizeIgnoreSignsEqualAbsoluteValuesReturnsSecondFirstTest() {
        final BigNumber[] sorted = MathTools.sortBySizeIgnoreSigns(
                new BigNumber("5", Locale.GERMAN),
                new BigNumber("-5", Locale.GERMAN)
        );
        assertEquals("-5", sorted[0].toString());
        assertEquals("5", sorted[1].toString());
    }

    @Test
    public void charAsIntTest() {
        assertEquals(0, MathTools.charAsInt('0'));
        assertEquals(5, MathTools.charAsInt('5'));
        assertEquals(9, MathTools.charAsInt('9'));
    }

    @Test
    public void getSignAsStringNegativeTest() {
        assertEquals("-", MathTools.getSignAsString(new BigNumber("-5", Locale.GERMAN)));
    }

    @Test
    public void getSignAsStringPositiveTest() {
        assertEquals("+", MathTools.getSignAsString(new BigNumber("5", Locale.GERMAN)));
    }

    // makeNumbersSameLength: vorne und hinten mit Nullen auffüllen
    @Test
    public void makeNumbersSameLengthPadsIntegerPartTest() {
        assertEquals(
                "005,0",
                MathTools.makeNumbersSameLength(
                        new BigNumber("5", Locale.GERMAN),
                        new BigNumber("123", Locale.GERMAN),
                        Locale.GERMAN
                )
        );
    }

    @Test
    public void makeNumbersSameLengthPadsDecimalPartTest() {
        assertEquals(
                "5,00",
                MathTools.makeNumbersSameLength(
                        new BigNumber("5", Locale.GERMAN),
                        new BigNumber("1,25", Locale.GERMAN),
                        Locale.GERMAN
                )
        );
    }

    @Test
    public void makeNumbersSameLengthPadsBothPartsTest() {
        assertEquals(
                "001,500",
                MathTools.makeNumbersSameLength(
                        new BigNumber("1,5", Locale.GERMAN),
                        new BigNumber("123,125", Locale.GERMAN),
                        Locale.GERMAN
                )
        );
    }

    @Test
    public void makeNumbersSameLengthLongerNumberStaysUnchangedTest() {
        assertEquals(
                "123,125",
                MathTools.makeNumbersSameLength(
                        new BigNumber("123,125", Locale.GERMAN),
                        new BigNumber("1,5", Locale.GERMAN),
                        Locale.GERMAN
                )
        );
    }

    @Test
    public void makeNumbersSameLengthEqualLengthsStayUnchangedTest() {
        assertEquals(
                "12,34",
                MathTools.makeNumbersSameLength(
                        new BigNumber("12,34", Locale.GERMAN),
                        new BigNumber("56,78", Locale.GERMAN),
                        Locale.GERMAN
                )
        );
    }

    @Test
    public void makeNumbersSameLengthIgnoresSignTest() {
        assertEquals(
                "005,0",
                MathTools.makeNumbersSameLength(
                        new BigNumber("-5", Locale.GERMAN),
                        new BigNumber("123", Locale.GERMAN),
                        Locale.GERMAN
                )
        );
    }

    @Test
    public void makeNumbersSameLengthWithEnglishTargetLocaleTest() {
        assertEquals(
                "5.00",
                MathTools.makeNumbersSameLength(
                        new BigNumber("5", Locale.GERMAN),
                        new BigNumber("1,25", Locale.GERMAN),
                        Locale.US
                )
        );
    }

    @Test
    public void makeNumbersSameLengthWithDifferentInputLocalesTest() {
        assertEquals(
                "01,50",
                MathTools.makeNumbersSameLength(
                        new BigNumber("1,5", Locale.GERMAN),
                        new BigNumber("12.25", Locale.US),
                        Locale.GERMAN
                )
        );
    }

    // getZeroNTimes
    @Test
    public void getZeroNTimesZeroTest() {
        assertEquals("", MathTools.getZeroNTimes(0));
    }

    @Test
    public void getZeroNTimesOneTest() {
        assertEquals("0", MathTools.getZeroNTimes(1));
    }

    @Test
    public void getZeroNTimesFiveTest() {
        assertEquals("00000", MathTools.getZeroNTimes(5));
    }

    // makeNumbersSameLength verlässt sich darauf, dass eine negative Anzahl nichts anhängt
    @Test
    public void getZeroNTimesNegativeTest() {
        assertEquals("", MathTools.getZeroNTimes(-3));
    }

    // signForMultiplicationResult
    @Test
    public void signForMultiplicationResultBothPositiveTest() {
        assertEquals("+", MathTools.signForMultiplicationResult(false, false));
    }

    @Test
    public void signForMultiplicationResultFirstNegativeTest() {
        assertEquals("-", MathTools.signForMultiplicationResult(true, false));
    }

    @Test
    public void signForMultiplicationResultSecondNegativeTest() {
        assertEquals("-", MathTools.signForMultiplicationResult(false, true));
    }

    @Test
    public void signForMultiplicationResultBothNegativeTest() {
        assertEquals("+", MathTools.signForMultiplicationResult(true, true));
    }

    // signForDivisionResult
    @Test
    public void signForDivisionResultBothPositiveTest() {
        assertEquals("+", MathTools.signForDivisionResult(false, false));
    }

    @Test
    public void signForDivisionResultDividendNegativeTest() {
        assertEquals("-", MathTools.signForDivisionResult(true, false));
    }

    @Test
    public void signForDivisionResultDivisorNegativeTest() {
        assertEquals("-", MathTools.signForDivisionResult(false, true));
    }

    @Test
    public void signForDivisionResultBothNegativeTest() {
        assertEquals("+", MathTools.signForDivisionResult(true, true));
    }

    // countDecimalPlaces: eine ganze Zahl hat 0 Nachkommastellen, obwohl intern "0" gespeichert ist
    @Test
    public void countDecimalPlacesIntegerTest() {
        assertEquals(0, MathTools.countDecimalPlaces(new BigNumber("5", Locale.GERMAN)));
    }

    @Test
    public void countDecimalPlacesZeroTest() {
        assertEquals(0, MathTools.countDecimalPlaces(new BigNumber("0", Locale.GERMAN)));
    }

    @Test
    public void countDecimalPlacesIntegerWithTrailingZeroTest() {
        assertEquals(0, MathTools.countDecimalPlaces(new BigNumber("10", Locale.GERMAN)));
    }

    @Test
    public void countDecimalPlacesIntegerWithZeroDecimalTest() {
        assertEquals(0, MathTools.countDecimalPlaces(new BigNumber("1,0", Locale.GERMAN)));
    }

    @Test
    public void countDecimalPlacesOneDecimalPlaceTest() {
        assertEquals(1, MathTools.countDecimalPlaces(new BigNumber("1,5", Locale.GERMAN)));
    }

    @Test
    public void countDecimalPlacesTwoDecimalPlacesTest() {
        assertEquals(2, MathTools.countDecimalPlaces(new BigNumber("1,25", Locale.GERMAN)));
    }

    @Test
    public void countDecimalPlacesNegativeValueTest() {
        assertEquals(2, MathTools.countDecimalPlaces(new BigNumber("-1,25", Locale.GERMAN)));
    }

    @Test
    public void countDecimalPlacesTrailingZerosAreNotCountedTest() {
        assertEquals(1, MathTools.countDecimalPlaces(new BigNumber("1,50", Locale.GERMAN)));
    }

    @Test
    public void countDecimalPlacesVerySmallDecimalTest() {
        assertEquals(8, MathTools.countDecimalPlaces(new BigNumber("0,00000001", Locale.GERMAN)));
    }

    @Test
    public void countDecimalPlacesWithGroupingSeparatorTest() {
        assertEquals(1, MathTools.countDecimalPlaces(new BigNumber("1.000,5", Locale.GERMAN)));
    }

    // sortBySizeIgnoreSigns
    @Test
    public void sortBySizeIgnoreSignsDecimalsTest() {
        final BigNumber[] sorted = MathTools.sortBySizeIgnoreSigns(
                new BigNumber("1,25", Locale.GERMAN),
                new BigNumber("1,5", Locale.GERMAN)
        );
        assertEquals("1,5", sorted[0].toString(Locale.GERMAN));
        assertEquals("1,25", sorted[1].toString(Locale.GERMAN));
    }

    @Test
    public void sortBySizeIgnoreSignsBothNegativeTest() {
        final BigNumber[] sorted = MathTools.sortBySizeIgnoreSigns(
                new BigNumber("-3", Locale.GERMAN),
                new BigNumber("-10", Locale.GERMAN)
        );
        assertEquals("-10", sorted[0].toString());
        assertEquals("-3", sorted[1].toString());
    }

    // getSignAsString
    @Test
    public void getSignAsStringZeroTest() {
        assertEquals("+", MathTools.getSignAsString(new BigNumber("0", Locale.GERMAN)));
    }

    @Test
    public void getSignAsStringNegativeZeroTest() {
        assertEquals("+", MathTools.getSignAsString(new BigNumber("-0", Locale.GERMAN)));
    }

    @Test
    public void getSignAsStringNegativeDecimalTest() {
        assertEquals("-", MathTools.getSignAsString(new BigNumber("-0,5", Locale.GERMAN)));
    }

}
