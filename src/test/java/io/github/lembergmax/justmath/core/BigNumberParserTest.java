package io.github.lembergmax.justmath.core.core;

import io.github.lembergmax.bignumber.BigNumber;
import io.github.lembergmax.exception.DuplicateDecimalSeparatorException;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BigNumberParserTest {

    @Test
    public void languageGermanTest() {
        // ohne Komma

        assertEquals("0", new BigNumber("", Locale.GERMAN).toString());
        assertEquals("1", new BigNumber("1", Locale.GERMAN).toString());
        assertEquals("12", new BigNumber("12", Locale.GERMAN).toString());
        assertEquals("123", new BigNumber("123", Locale.GERMAN).toString());

        assertEquals("1234", new BigNumber("1234", Locale.GERMAN).toString());
        assertEquals("1234", new BigNumber("1.234", Locale.GERMAN).toString());

        assertEquals("12345", new BigNumber("12345", Locale.GERMAN).toString());
        assertEquals("12345", new BigNumber("12.345", Locale.GERMAN).toString());

        assertEquals("123456", new BigNumber("123.456", Locale.GERMAN).toString());
        assertEquals("123456", new BigNumber("123456", Locale.GERMAN).toString());

        assertEquals("1234567", new BigNumber("1234567", Locale.GERMAN).toString());
        assertEquals("1234567", new BigNumber("1.234.567", Locale.GERMAN).toString());

        // mit Komma
        assertEquals("0,0001", new BigNumber("0,0001", Locale.GERMAN).toString());
        assertEquals("0,001", new BigNumber("0,001", Locale.GERMAN).toString());
        assertEquals("0,01", new BigNumber("0,01", Locale.GERMAN).toString());
        assertEquals("0,1", new BigNumber("0,1", Locale.GERMAN).toString());

        assertEquals("1,1", new BigNumber("1,1", Locale.GERMAN).toString());
        assertEquals("12,12", new BigNumber("12,12", Locale.GERMAN).toString());
        assertEquals("123,123", new BigNumber("123,123", Locale.GERMAN).toString());

        assertEquals("1234,1234", new BigNumber("1234,1234", Locale.GERMAN).toString());
        assertEquals("1234,1234", new BigNumber("1.234,1234", Locale.GERMAN).toString());

        assertEquals("12345,12345", new BigNumber("12345,12345", Locale.GERMAN).toString());
        assertEquals("12345,12345", new BigNumber("12.345,12345", Locale.GERMAN).toString());

        assertEquals("123456,123456", new BigNumber("123.456,123456", Locale.GERMAN).toString());
        assertEquals("123456,123456", new BigNumber("123456,123456", Locale.GERMAN).toString());

        assertEquals("1234567,1234567", new BigNumber("1234567,1234567", Locale.GERMAN).toString());
        assertEquals("1234567,1234567", new BigNumber("1.234.567,1234567", Locale.GERMAN).toString());

        assertEquals("50000000000", new BigNumber("50000000000", Locale.GERMAN).toString());
    }

    @Test
    public void languageEnglishTest() {
        // ohne Komma
        //assertEquals("0",                   new BigNumber("", Locale.ENGLISH).toString());
        assertEquals("1", new BigNumber("1", Locale.ENGLISH).toString());
        assertEquals("12", new BigNumber("12", Locale.ENGLISH).toString());
        assertEquals("123", new BigNumber("123", Locale.ENGLISH).toString());

        assertEquals("1234", new BigNumber("1234", Locale.ENGLISH).toString());
        assertEquals("1234", new BigNumber("1,234", Locale.ENGLISH).toString());

        assertEquals("12345", new BigNumber("12345", Locale.ENGLISH).toString());
        assertEquals("12345", new BigNumber("12,345", Locale.ENGLISH).toString());

        assertEquals("123456", new BigNumber("123,456", Locale.ENGLISH).toString());
        assertEquals("123456", new BigNumber("123456", Locale.ENGLISH).toString());

        assertEquals("1234567", new BigNumber("1234567", Locale.ENGLISH).toString());
        assertEquals("1234567", new BigNumber("1,234,567", Locale.ENGLISH).toString());

        // mit Komma
        assertEquals("0.0001", new BigNumber("0.0001", Locale.ENGLISH).toString());
        assertEquals("0.001", new BigNumber("0.001", Locale.ENGLISH).toString());
        assertEquals("0.01", new BigNumber("0.01", Locale.ENGLISH).toString());
        assertEquals("0.1", new BigNumber("0.1", Locale.ENGLISH).toString());

        assertEquals("1.1", new BigNumber("1.1", Locale.ENGLISH).toString());
        assertEquals("12.12", new BigNumber("12.12", Locale.ENGLISH).toString());
        assertEquals("123.123", new BigNumber("123.123", Locale.ENGLISH).toString());

        assertEquals("1234.1234", new BigNumber("1234.1234", Locale.ENGLISH).toString());
        assertEquals("1234.1234", new BigNumber("1,234.1234", Locale.ENGLISH).toString());

        assertEquals("12345.12345", new BigNumber("12345.12345", Locale.ENGLISH).toString());
        assertEquals("12345.12345", new BigNumber("12,345.12345", Locale.ENGLISH).toString());

        assertEquals("123456.123456", new BigNumber("123,456.123456", Locale.ENGLISH).toString());
        assertEquals("123456.123456", new BigNumber("123456.123456", Locale.ENGLISH).toString());

        assertEquals("1234567.1234567", new BigNumber("1234567.1234567", Locale.ENGLISH).toString());
        assertEquals("1234567.1234567", new BigNumber("1,234,567.1234567", Locale.ENGLISH).toString());

        assertEquals("50000000000", new BigNumber("50000000000", Locale.ENGLISH).toString());
    }

    @Test
    public void leadingAndTrailingZerosTest() {
        assertEquals("1", new BigNumber("00000000000001", Locale.GERMAN).toString());
        assertEquals("0,000000000001", new BigNumber("0,000000000001", Locale.GERMAN).toString());
        assertEquals("0", new BigNumber("0,000000000000", Locale.GERMAN).toString());
        assertEquals("0", new BigNumber("000000,0000000", Locale.GERMAN).toString());
        assertEquals("0", new BigNumber("000000,0000000", Locale.GERMAN).toString());
        assertEquals("0", new BigNumber("00000000000000", Locale.GERMAN).toString());
        assertEquals("0", new BigNumber("00000000000000", Locale.GERMAN).toString());
    }

    @Test
    public void randomCharactersTest() {
        assertEquals("0", new BigNumber(",", Locale.GERMAN).toString());
        assertEquals("0", new BigNumber(".", Locale.GERMAN).toString());
        assertEquals("0", new BigNumber(",.", Locale.GERMAN).toString());
        assertEquals("0", new BigNumber(".,", Locale.GERMAN).toString());
        assertEquals("0", new BigNumber(".,.", Locale.GERMAN).toString());
        assertEquals("0", new BigNumber("...", Locale.GERMAN).toString());

        assertThrows(DuplicateDecimalSeparatorException.class, () -> new BigNumber(",,.", Locale.GERMAN).toString());
        assertThrows(DuplicateDecimalSeparatorException.class, () -> new BigNumber(",,.", Locale.GERMAN).toString());
        assertThrows(DuplicateDecimalSeparatorException.class, () -> new BigNumber(".,,", Locale.GERMAN).toString());
        assertThrows(DuplicateDecimalSeparatorException.class, () -> new BigNumber(",,,", Locale.GERMAN).toString());
    }

}
