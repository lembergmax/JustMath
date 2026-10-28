package io.github.lembergmax.justmath.bignumber.math;

import org.junit.jupiter.api.Test;

import io.github.lembergmax.justmath.bignumber.BigNumber;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    public void subtractNegativeValues() {
        assertEquals(
                "-988,96",
                BasicMath.subtract(
                        new BigNumber("-2512,78", Locale.GERMAN),
                        new BigNumber("-1523,82", Locale.GERMAN)
                ).toString()
        );
    }
}
