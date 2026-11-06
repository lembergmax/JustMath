package io.github.lembergmax.justmath.bignumber;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertEquals(new BigNumber("2,5", Locale.GERMAN), new BigNumber("2.5", Locale.US));
    }

    @Test
    public void equalsAgainstNonBigNumberIsFalseTest() {
        assertNotEquals(new BigNumber("5", Locale.GERMAN), "5");
    }

    @Test
    public void equalsAgainstNullIsFalseTest() {
        assertNotEquals(new BigNumber("5", Locale.GERMAN), null);
    }

    // hashCode muss zu equals passen, sonst finden HashSet und HashMap gleiche Werte nicht
    @Test
    public void hashCodeSameValueIsEqualTest() {
        assertEquals(
                new BigNumber("5", Locale.GERMAN).hashCode(),
                new BigNumber("5", Locale.GERMAN).hashCode()
        );
    }

    @Test
    public void hashCodeWithTrailingZeroIsEqualTest() {
        assertEquals(
                new BigNumber("1,5", Locale.GERMAN).hashCode(),
                new BigNumber("1,50", Locale.GERMAN).hashCode()
        );
    }

    @Test
    public void hashCodeWithLeadingZeroIsEqualTest() {
        assertEquals(
                new BigNumber("007", Locale.GERMAN).hashCode(),
                new BigNumber("7", Locale.GERMAN).hashCode()
        );
    }

    @Test
    public void hashCodeWithPositiveSignIsEqualTest() {
        assertEquals(
                new BigNumber("+5", Locale.GERMAN).hashCode(),
                new BigNumber("5", Locale.GERMAN).hashCode()
        );
    }

    @Test
    public void hashCodeNegativeZeroAndZeroIsEqualTest() {
        assertEquals(
                new BigNumber("-0", Locale.GERMAN).hashCode(),
                new BigNumber("0", Locale.GERMAN).hashCode()
        );
    }

    @Test
    public void hashCodeDifferentLocalesSameValueIsEqualTest() {
        assertEquals(
                new BigNumber("2,5", Locale.GERMAN).hashCode(),
                new BigNumber("2.5", Locale.US).hashCode()
        );
    }

    @Test
    public void hashCodeIsStableTest() {
        final BigNumber number = new BigNumber("2,5", Locale.GERMAN);

        assertEquals(number.hashCode(), number.hashCode());
    }

    @Test
    public void hashCodeHashSetFindsEqualValueTest() {
        final Set<BigNumber> numbers = new HashSet<>();
        numbers.add(new BigNumber("2,5", Locale.GERMAN));

        assertTrue(numbers.contains(new BigNumber("2,50", Locale.GERMAN)));
    }

    @Test
    public void hashCodeHashSetKeepsEqualValuesOnlyOnceTest() {
        final Set<BigNumber> numbers = new HashSet<>();
        numbers.add(new BigNumber("5", Locale.GERMAN));
        numbers.add(new BigNumber("5,0", Locale.GERMAN));

        assertEquals(1, numbers.size());
    }

}
