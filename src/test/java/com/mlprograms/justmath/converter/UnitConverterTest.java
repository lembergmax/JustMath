package com.mlprograms.justmath.converter;

import com.mlprograms.justmath.bignumber.BigNumber;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UnitConverterTest {

    @Test
    void shouldConvertCentimeterToFeetUsingSymbols() {
        BigNumber result = UnitConverter.convert(new BigNumber("100"), "cm", "ft");

        assertEquals("3.28083989501312335958005249343832", result.toString());
    }

    @Test
    void shouldConvertUsingAliasesCaseInsensitive() {
        BigNumber result = UnitConverter.convert(new BigNumber("250"), "CENTIMETER", "Feet");

        assertEquals("8.202099737532808398950131233595801", result.toString());
    }

    @Test
    void shouldFailForUnknownUnit() {
        assertThrows(IllegalArgumentException.class, () -> UnitConverter.convert(new BigNumber("1"), "cm", "banana"));
    }

}
