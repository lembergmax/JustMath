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
                        new BigNumber("123,12345", Locale.GERMAN)
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

}
