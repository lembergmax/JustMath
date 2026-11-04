package io.github.lembergmax.justmath.bignumber;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class BigNumberEqualsTest {

    @Test
    public void equalsSameValueIsTrueTest() {
        assertEquals(new BigNumber("5", Locale.GERMAN), new BigNumber("5", Locale.GERMAN));
    }

    @Test
    public void equalsDifferentValueIsFalseTest() {
        assertNotEquals(new BigNumber("5", Locale.GERMAN), new BigNumber("6", Locale.GERMAN));
    }

    @Test
    public void equalsWithTrailingZeroIsTrueTest() {
        assertEquals(new BigNumber("1,5", Locale.GERMAN), new BigNumber("1,50", Locale.GERMAN));
    }

    @Test
    public void equalsWithLeadingZeroIsTrueTest() {
        assertEquals(new BigNumber("007", Locale.GERMAN), new BigNumber("7", Locale.GERMAN));
    }

    @Test
    public void equalsWithPositiveSignIsTrueTest() {
        assertEquals(new BigNumber("+5", Locale.GERMAN), new BigNumber("5", Locale.GERMAN));
    }

    @Test
    public void equalsNegativeZeroAndZeroIsTrueTest() {
        assertEquals(new BigNumber("-0", Locale.GERMAN), new BigNumber("0", Locale.GERMAN));
    }

    // Die Gleichheit basiert auf der formatierten String-Darstellung, daher zählt auch der Dezimaltrenner der Locale
    @Test
    public void equalsDifferentLocalesSameValueIsFalseTest() {
        assertNotEquals(new BigNumber("2,5", Locale.GERMAN), new BigNumber("2.5", Locale.US));
    }

    @Test
    public void equalsAgainstNonBigNumberIsFalseTest() {
        assertNotEquals(new BigNumber("5", Locale.GERMAN), "5");
    }

    @Test
    public void equalsAgainstNullIsFalseTest() {
        assertNotEquals(new BigNumber("5", Locale.GERMAN), null);
    }

}
