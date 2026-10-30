package io.github.lembergmax.justmath.bignumber;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class BigNumberTest {

    @Test
    public void greaterThanTest() {
        assertFalse(new BigNumber("2415", Locale.GERMAN).greaterThan(new BigNumber("3276", Locale.GERMAN)));
        assertTrue(new BigNumber("321", Locale.GERMAN).greaterThan(new BigNumber("314", Locale.GERMAN)));
    }

    @Test
    public void smallerThanTest() {
        assertTrue(new BigNumber("2425", Locale.GERMAN).smallerThan(new BigNumber("3276", Locale.GERMAN)));
        assertFalse(new BigNumber("321", Locale.GERMAN).smallerThan(new BigNumber("314", Locale.GERMAN)));
    }

    @Test
    public void greaterThanDifferentLengthsTest() {
        assertAll(
                () -> assertTrue(german("1000").greaterThan(german("999"))),
                () -> assertFalse(german("999").greaterThan(german("1000"))),
                () -> assertTrue(german("10").greaterThan(german("9")))
        );
    }

    @Test
    public void smallerThanDifferentLengthsTest() {
        assertAll(
                () -> assertTrue(german("999").smallerThan(german("1000"))),
                () -> assertFalse(german("1000").smallerThan(german("999"))),
                () -> assertTrue(german("9").smallerThan(german("10")))
        );
    }

    @Test
    public void greaterThanDecimalsTest() {
        assertAll(
                () -> assertTrue(german("1,5").greaterThan(german("1,25"))),
                () -> assertFalse(german("1,25").greaterThan(german("1,5"))),
                () -> assertTrue(german("1,0002").greaterThan(german("1,0001"))),
                () -> assertFalse(german("1,0001").greaterThan(german("1,0002"))),
                () -> assertTrue(german("0,5").greaterThan(german("0,05"))),
                () -> assertFalse(german("0,05").greaterThan(german("0,5"))),
                () -> assertTrue(german("2").greaterThan(german("1,999"))),
                () -> assertFalse(german("1,999").greaterThan(german("2")))
        );
    }

    @Test
    public void smallerThanDecimalsTest() {
        assertAll(
                () -> assertTrue(german("1,25").smallerThan(german("1,5"))),
                () -> assertFalse(german("1,5").smallerThan(german("1,25"))),
                () -> assertTrue(german("1,0001").smallerThan(german("1,0002"))),
                () -> assertFalse(german("1,0002").smallerThan(german("1,0001"))),
                () -> assertTrue(german("0,05").smallerThan(german("0,5"))),
                () -> assertFalse(german("0,5").smallerThan(german("0,05"))),
                () -> assertTrue(german("1,999").smallerThan(german("2"))),
                () -> assertFalse(german("2").smallerThan(german("1,999")))
        );
    }

    // Gleiche Werte sind weder größer noch kleiner
    @Test
    public void greaterThanEqualValuesTest() {
        assertAll(
                () -> assertFalse(german("5").greaterThan(german("5"))),
                () -> assertFalse(german("123,456").greaterThan(german("123,456"))),
                () -> assertFalse(german("1,50").greaterThan(german("1,5"))),
                () -> assertFalse(german("007").greaterThan(german("7"))),
                () -> assertFalse(german("+5").greaterThan(german("5"))),
                () -> assertFalse(german("1.000").greaterThan(german("1000"))),
                () -> assertFalse(german("-5").greaterThan(german("-5"))),
                () -> assertFalse(german("-1,5").greaterThan(german("-1,50")))
        );
    }

    @Test
    public void smallerThanEqualValuesTest() {
        assertAll(
                () -> assertFalse(german("5").smallerThan(german("5"))),
                () -> assertFalse(german("123,456").smallerThan(german("123,456"))),
                () -> assertFalse(german("1,50").smallerThan(german("1,5"))),
                () -> assertFalse(german("007").smallerThan(german("7"))),
                () -> assertFalse(german("+5").smallerThan(german("5"))),
                () -> assertFalse(german("1.000").smallerThan(german("1000"))),
                () -> assertFalse(german("-5").smallerThan(german("-5"))),
                () -> assertFalse(german("-1,5").smallerThan(german("-1,50")))
        );
    }

    @Test
    public void greaterThanZeroTest() {
        assertAll(
                () -> assertFalse(german("0").greaterThan(german("0"))),
                () -> assertTrue(german("5").greaterThan(german("0"))),
                () -> assertFalse(german("0").greaterThan(german("5"))),
                () -> assertTrue(german("0,0001").greaterThan(german("0"))),
                () -> assertFalse(german("0").greaterThan(german("0,0001")))
        );
    }

    @Test
    public void smallerThanZeroTest() {
        assertAll(
                () -> assertFalse(german("0").smallerThan(german("0"))),
                () -> assertTrue(german("0").smallerThan(german("5"))),
                () -> assertFalse(german("5").smallerThan(german("0"))),
                () -> assertTrue(german("0").smallerThan(german("0,0001"))),
                () -> assertFalse(german("0,0001").smallerThan(german("0")))
        );
    }

    // -0 hat denselben Wert wie 0
    @Test
    public void greaterThanNegativeZeroTest() {
        assertAll(
                () -> assertFalse(german("-0").greaterThan(german("0"))),
                () -> assertFalse(german("0").greaterThan(german("-0"))),
                () -> assertFalse(german("-0").greaterThan(german("-0"))),
                () -> assertTrue(german("-0").greaterThan(german("-5"))),
                () -> assertFalse(german("-5").greaterThan(german("-0"))),
                () -> assertFalse(german("-0").greaterThan(german("5"))),
                () -> assertTrue(german("5").greaterThan(german("-0")))
        );
    }

    @Test
    public void smallerThanNegativeZeroTest() {
        assertAll(
                () -> assertFalse(german("-0").smallerThan(german("0"))),
                () -> assertFalse(german("0").smallerThan(german("-0"))),
                () -> assertFalse(german("-0").smallerThan(german("-0"))),
                () -> assertTrue(german("-5").smallerThan(german("-0"))),
                () -> assertFalse(german("-0").smallerThan(german("-5"))),
                () -> assertTrue(german("-0").smallerThan(german("5"))),
                () -> assertFalse(german("5").smallerThan(german("-0")))
        );
    }

    // + -
    @Test
    public void greaterThanDifferentSignsTest() {
        assertAll(
                () -> assertTrue(german("5").greaterThan(german("-3"))),
                () -> assertTrue(german("3").greaterThan(german("-5"))),
                () -> assertFalse(german("-5").greaterThan(german("3"))),
                () -> assertFalse(german("-3").greaterThan(german("5"))),
                () -> assertTrue(german("5").greaterThan(german("-5"))),
                () -> assertFalse(german("-5").greaterThan(german("5"))),
                () -> assertTrue(german("0").greaterThan(german("-0,1"))),
                () -> assertFalse(german("-0,1").greaterThan(german("0")))
        );
    }

    // + -
    @Test
    public void smallerThanDifferentSignsTest() {
        assertAll(
                () -> assertTrue(german("-3").smallerThan(german("5"))),
                () -> assertTrue(german("-5").smallerThan(german("3"))),
                () -> assertFalse(german("3").smallerThan(german("-5"))),
                () -> assertFalse(german("5").smallerThan(german("-3"))),
                () -> assertTrue(german("-5").smallerThan(german("5"))),
                () -> assertFalse(german("5").smallerThan(german("-5"))),
                () -> assertTrue(german("-0,1").smallerThan(german("0"))),
                () -> assertFalse(german("0").smallerThan(german("-0,1")))
        );
    }

    // - -
    @Test
    public void greaterThanBothNegativeTest() {
        assertAll(
                () -> assertTrue(german("-3").greaterThan(german("-5"))),
                () -> assertFalse(german("-5").greaterThan(german("-3"))),
                () -> assertTrue(german("-999").greaterThan(german("-1000"))),
                () -> assertFalse(german("-1000").greaterThan(german("-999"))),
                () -> assertTrue(german("-1,25").greaterThan(german("-1,5"))),
                () -> assertFalse(german("-1,5").greaterThan(german("-1,25"))),
                () -> assertTrue(german("-0,05").greaterThan(german("-0,5"))),
                () -> assertFalse(german("-0,5").greaterThan(german("-0,05")))
        );
    }

    // - -
    @Test
    public void smallerThanBothNegativeTest() {
        assertAll(
                () -> assertTrue(german("-5").smallerThan(german("-3"))),
                () -> assertFalse(german("-3").smallerThan(german("-5"))),
                () -> assertTrue(german("-1000").smallerThan(german("-999"))),
                () -> assertFalse(german("-999").smallerThan(german("-1000"))),
                () -> assertTrue(german("-1,5").smallerThan(german("-1,25"))),
                () -> assertFalse(german("-1,25").smallerThan(german("-1,5"))),
                () -> assertTrue(german("-0,5").smallerThan(german("-0,05"))),
                () -> assertFalse(german("-0,05").smallerThan(german("-0,5")))
        );
    }

    @Test
    public void greaterThanVeryLargeValuesTest() {
        assertAll(
                () -> assertTrue(german("1000000000000000000000000000001").greaterThan(german("1000000000000000000000000000000"))),
                () -> assertFalse(german("1000000000000000000000000000000").greaterThan(german("1000000000000000000000000000001"))),
                () -> assertTrue(german("-1000000000000000000000000000000").greaterThan(german("-1000000000000000000000000000001"))),
                () -> assertFalse(german("-1000000000000000000000000000001").greaterThan(german("-1000000000000000000000000000000")))
        );
    }

    @Test
    public void smallerThanVeryLargeValuesTest() {
        assertAll(
                () -> assertTrue(german("1000000000000000000000000000000").smallerThan(german("1000000000000000000000000000001"))),
                () -> assertFalse(german("1000000000000000000000000000001").smallerThan(german("1000000000000000000000000000000"))),
                () -> assertTrue(german("-1000000000000000000000000000001").smallerThan(german("-1000000000000000000000000000000"))),
                () -> assertFalse(german("-1000000000000000000000000000000").smallerThan(german("-1000000000000000000000000000001")))
        );
    }

    @Test
    public void greaterThanEnglishLocaleTest() {
        assertAll(
                () -> assertTrue(english("1.5").greaterThan(english("1.25"))),
                () -> assertFalse(english("1.25").greaterThan(english("1.5"))),
                () -> assertFalse(english("1.5").greaterThan(english("1.5"))),
                () -> assertTrue(english("1,000").greaterThan(english("999"))),
                () -> assertTrue(english("-1.25").greaterThan(english("-1.5"))),
                () -> assertFalse(english("-1.5").greaterThan(english("-1.25")))
        );
    }

    @Test
    public void smallerThanEnglishLocaleTest() {
        assertAll(
                () -> assertTrue(english("1.25").smallerThan(english("1.5"))),
                () -> assertFalse(english("1.5").smallerThan(english("1.25"))),
                () -> assertFalse(english("1.5").smallerThan(english("1.5"))),
                () -> assertTrue(english("999").smallerThan(english("1,000"))),
                () -> assertTrue(english("-1.5").smallerThan(english("-1.25"))),
                () -> assertFalse(english("-1.25").smallerThan(english("-1.5")))
        );
    }

    @Test
    public void greaterThanDifferentLocalesTest() {
        assertAll(
                () -> assertFalse(german("2,5").greaterThan(english("2.5"))),
                () -> assertTrue(german("2,5").greaterThan(english("2.25"))),
                () -> assertFalse(english("2.25").greaterThan(german("2,5"))),
                () -> assertTrue(german("-3,5").greaterThan(english("-4.5"))),
                () -> assertFalse(german("-4,5").greaterThan(english("-3.5")))
        );
    }

    @Test
    public void smallerThanDifferentLocalesTest() {
        assertAll(
                () -> assertFalse(german("2,5").smallerThan(english("2.5"))),
                () -> assertTrue(english("2.25").smallerThan(german("2,5"))),
                () -> assertFalse(german("2,5").smallerThan(english("2.25"))),
                () -> assertTrue(german("-4,5").smallerThan(english("-3.5"))),
                () -> assertFalse(german("-3,5").smallerThan(english("-4.5")))
        );
    }

    // Ein Vergleich darf die Operanden nicht verändern
    @Test
    public void greaterThanDoesNotChangeOperandsTest() {
        final BigNumber negativeZero = german("-0");

        negativeZero.greaterThan(german("-5"));

        assertTrue(negativeZero.isNegative());
    }

    @Test
    public void smallerThanDoesNotChangeOperandsTest() {
        final BigNumber negativeZero = german("-0");

        negativeZero.smallerThan(german("-5"));

        assertTrue(negativeZero.isNegative());
    }

    private static BigNumber german(final String number) {
        return new BigNumber(number, Locale.GERMAN);
    }

    private static BigNumber english(final String number) {
        return new BigNumber(number, Locale.US);
    }
}
