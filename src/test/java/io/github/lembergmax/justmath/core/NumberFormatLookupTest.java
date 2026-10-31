package io.github.lembergmax.justmath.core;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NumberFormatLookupTest {

    @Test
    public void decimalSeparatorForGermanLocaleTest() {
        assertEquals(",", NumberFormatLookup.getDecimalSeparatorForLocale(Locale.GERMAN));
    }

    @Test
    public void decimalSeparatorForEnglishLocaleTest() {
        assertEquals(".", NumberFormatLookup.getDecimalSeparatorForLocale(Locale.US));
    }

    @Test
    public void groupingSeparatorForGermanLocaleTest() {
        assertEquals(".", NumberFormatLookup.getGroupingSeparatorForLocale(Locale.GERMAN));
    }

    @Test
    public void groupingSeparatorForEnglishLocaleTest() {
        assertEquals(",", NumberFormatLookup.getGroupingSeparatorForLocale(Locale.US));
    }

}
