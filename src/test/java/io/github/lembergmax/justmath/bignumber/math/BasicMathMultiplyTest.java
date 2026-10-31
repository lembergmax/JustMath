package io.github.lembergmax.justmath.bignumber.math;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class BasicMathMultiplyTest {

    @Test
    public void multiplyBothPositiveTest() {
        assertEquals(
                "60",
                BasicMath.multiply(
                        new BigNumber("12", Locale.GERMAN),
                        new BigNumber("5", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void multiplyByZeroTest() {
        assertEquals(
                "0",
                BasicMath.multiply(
                        new BigNumber("12345", Locale.GERMAN),
                        new BigNumber("0", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void multiplyZeroByZeroTest() {
        assertEquals(
                "0",
                BasicMath.multiply(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("0", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void multiplyFirstNegativeTest() {
        assertEquals(
                "-60",
                BasicMath.multiply(
                        new BigNumber("-12", Locale.GERMAN),
                        new BigNumber("5", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void multiplySecondNegativeTest() {
        assertEquals(
                "-60",
                BasicMath.multiply(
                        new BigNumber("12", Locale.GERMAN),
                        new BigNumber("-5", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void multiplyBothNegativeTest() {
        assertEquals(
                "60",
                BasicMath.multiply(
                        new BigNumber("-12", Locale.GERMAN),
                        new BigNumber("-5", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void multiplyWithDecimalsTest() {
        assertEquals(
                "2,5",
                BasicMath.multiply(
                        new BigNumber("1,25", Locale.GERMAN),
                        new BigNumber("2", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void multiplyDecimalsWithTrailingZeroTest() {
        assertEquals(
                "0,05",
                BasicMath.multiply(
                        new BigNumber("0,1", Locale.GERMAN),
                        new BigNumber("0,5", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void multiplyCarryOverMultipleDigitsTest() {
        assertEquals(
                "9876401235",
                BasicMath.multiply(
                        new BigNumber("99999", Locale.GERMAN),
                        new BigNumber("98765", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void multiplyByOneTest() {
        assertEquals(
                "12345",
                BasicMath.multiply(
                        new BigNumber("12345", Locale.GERMAN),
                        new BigNumber("1", Locale.GERMAN),
                        Locale.GERMAN
                ).toString()
        );
    }

    @Test
    public void multiplyResultIsNotNegativeWhenBothNegativeTest() {
        assertFalse(
                BasicMath.multiply(
                        new BigNumber("-3", Locale.GERMAN),
                        new BigNumber("-4", Locale.GERMAN),
                        Locale.GERMAN
                ).isNegative()
        );
    }

    @Test
    public void multiplyWithEnglishLocaleTest() {
        assertEquals(
                "7.5",
                BasicMath.multiply(
                        new BigNumber("2.5", Locale.US),
                        new BigNumber("3", Locale.US),
                        Locale.US
                ).toString()
        );
    }

    @Test
    public void multiplyDoesNotChangeOperandsTest() {
        final BigNumber factor1 = new BigNumber("5", Locale.GERMAN);
        final BigNumber factor2 = new BigNumber("-8", Locale.GERMAN);

        BasicMath.multiply(factor1, factor2, factor1.getLocale());

        assertEquals("5", factor1.toString());
        assertEquals("-8", factor2.toString());
    }

}
