package io.github.lembergmax.justmath.bignumber;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BigNumberNegateTest {
    @Test
    public void negateOfPositiveIsNegativeTest() {
        assertEquals("-5", new BigNumber("5", Locale.GERMAN).negate().toString());
    }

    @Test
    public void negateOfNegativeIsPositiveTest() {
        assertEquals("5", new BigNumber("-5", Locale.GERMAN).negate().toString());
    }

    @Test
    public void negateOfPositiveHasNegativeSignTest() {
        assertTrue(new BigNumber("5", Locale.GERMAN).negate().isNegative());
    }

    @Test
    public void negateOfNegativeIsNotNegativeTest() {
        assertFalse(new BigNumber("-5", Locale.GERMAN).negate().isNegative());
    }

    @Test
    public void negateOfDecimalValueTest() {
        assertEquals("-1,25", new BigNumber("1,25", Locale.GERMAN).negate().toString(Locale.GERMAN));
    }

    @Test
    public void negateOfNegativeDecimalValueTest() {
        assertEquals("1,25", new BigNumber("-1,25", Locale.GERMAN).negate().toString(Locale.GERMAN));
    }

    @Test
    public void negateOfVerySmallDecimalTest() {
        assertEquals("-0,00000001", new BigNumber("0,00000001", Locale.GERMAN).negate().toString(Locale.GERMAN));
    }

    @Test
    public void negateOfVeryLargeValueTest() {
        assertEquals("-90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", new BigNumber("90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", Locale.GERMAN).negate().toString());
    }

    @Test
    public void negateWithEnglishLocaleTest() {
        assertEquals("-2.5", new BigNumber("2.5", Locale.US).negate().toString());
    }

    @Test
    public void negateTwiceGivesOriginalValueTest() {
        assertEquals("-2,5", new BigNumber("-2,5", Locale.GERMAN).negate().negate().toString(Locale.GERMAN));
    }

    // negate() tauscht das Vorzeichen immer, auch bei 0. Der Wert bleibt trotzdem 0.
    @Test
    public void negateOfZeroIsZeroTest() {
        assertEquals("0", new BigNumber("0", Locale.GERMAN).negate().toString());
    }

    @Test
    public void negateOfZeroSwapsSignTest() {
        assertTrue(new BigNumber("0", Locale.GERMAN).negate().isNegative());
    }

    @Test
    public void negateOfZeroWithDecimalsSwapsSignTest() {
        assertTrue(new BigNumber("0,000", Locale.GERMAN).negate().isNegative());
    }

    @Test
    public void negateOfZeroTwiceIsNotNegativeTest() {
        assertFalse(new BigNumber("0", Locale.GERMAN).negate().negate().isNegative());
    }

    // Der Parser speichert "-0" ohne Vorzeichen, negate() macht daraus also eine 0 mit Minus
    @Test
    public void negateOfParsedNegativeZeroSwapsSignTest() {
        assertTrue(new BigNumber("-0", Locale.GERMAN).negate().isNegative());
    }

    // Die negierte 0 trägt ein Minus: Vergleiche behandeln sie als kleiner als die 0 ohne Vorzeichen
    @Test
    public void negateOfZeroIsNotEqualToZeroTest() {
        assertFalse(new BigNumber("0", Locale.GERMAN).negate().isEqualTo(new BigNumber("0", Locale.GERMAN)));
    }

    @Test
    public void negateOfZeroIsSmallerThanZeroTest() {
        assertTrue(new BigNumber("0", Locale.GERMAN).negate().isSmallerThan(new BigNumber("0", Locale.GERMAN)));
    }

    @Test
    public void negateOfZeroIsNotGreaterThanZeroTest() {
        assertFalse(new BigNumber("0", Locale.GERMAN).negate().isGreaterThan(new BigNumber("0", Locale.GERMAN)));
    }

    @Test
    public void zeroIsGreaterThanNegatedZeroTest() {
        assertTrue(new BigNumber("0", Locale.GERMAN).isGreaterThan(new BigNumber("0", Locale.GERMAN).negate()));
    }

    // Eingabewerte dürfen nicht verändert werden
    @Test
    public void negateDoesNotChangeOperandTest() {
        final BigNumber negative = new BigNumber("-5", Locale.GERMAN);

        negative.negate();

        assertTrue(negative.isNegative());
    }

}
