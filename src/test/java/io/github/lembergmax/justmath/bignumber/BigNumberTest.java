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
}
