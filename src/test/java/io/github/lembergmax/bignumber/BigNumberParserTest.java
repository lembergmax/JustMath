package io.github.lembergmax.bignumber;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BigNumberParserTest {

    @Test
    public void numberParsingTest_LanguageGerman() {
        // ohne Komma
        assertEquals("0",                   new BigNumber("", Locale.GERMAN).toString());

        assertEquals("1",                   new BigNumber("1", Locale.GERMAN).toString());
        assertEquals("12",                  new BigNumber("12", Locale.GERMAN).toString());
        assertEquals("123",                 new BigNumber("123", Locale.GERMAN).toString());

        assertEquals("1234",                new BigNumber("1234", Locale.GERMAN).toString());
        assertEquals("1234",                new BigNumber("1.234", Locale.GERMAN).toString());

        assertEquals("12345",               new BigNumber("12345", Locale.GERMAN).toString());
        assertEquals("12345",               new BigNumber("12.345", Locale.GERMAN).toString());

        assertEquals("123456",              new BigNumber("123.456", Locale.GERMAN).toString());
        assertEquals("123456",              new BigNumber("123456", Locale.GERMAN).toString());

        assertEquals("1234567",             new BigNumber("1234567", Locale.GERMAN).toString());
        assertEquals("1234567",             new BigNumber("1.234.567", Locale.GERMAN).toString());

        // mit Komma
        assertEquals("1,1",                 new BigNumber("1,1", Locale.GERMAN).toString());
        assertEquals("12,12",               new BigNumber("12,12", Locale.GERMAN).toString());
        assertEquals("123,123",             new BigNumber("123,123", Locale.GERMAN).toString());

        assertEquals("1234,1234",           new BigNumber("1234,1234", Locale.GERMAN).toString());
        assertEquals("1234,1234",           new BigNumber("1.234,1234", Locale.GERMAN).toString());

        assertEquals("12345,12345",         new BigNumber("12345,12345", Locale.GERMAN).toString());
        assertEquals("12345,12345",         new BigNumber("12.345,12345", Locale.GERMAN).toString());

        assertEquals("123456,123456",       new BigNumber("123.456,123456", Locale.GERMAN).toString());
        assertEquals("123456,123456",       new BigNumber("123456,123456", Locale.GERMAN).toString());

        assertEquals("1234567,1234567",     new BigNumber("1234567,1234567", Locale.GERMAN).toString());
        assertEquals("1234567,1234567",     new BigNumber("1.234.567,1234567", Locale.GERMAN).toString());
    }

    @Test
    public void numberParsingTest_LanguageEnglish() {

    }

}
