/*
 * Copyright (c) 2026 Max Lemberg
 *
 * This file is part of JustMath.
 */

package com.mlprograms.justmath.converter;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.converter.unit.Unit;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UnitConverterTest {

    private static final MathContext MC = new MathContext(20);
    private final UnitConverter converter = new UnitConverter(MC);

    @Test
    void shouldConvertLengthUsingSharedBaseUnit() {
        BigNumber result = converter.convert(new BigNumber("1", Locale.US), Unit.LENGTH.MILE, Unit.LENGTH.KILOMETER);

        assertEquals(new BigDecimal("1.609344"), result.toBigDecimal().setScale(6, RoundingMode.HALF_UP));
    }

    @Test
    void shouldConvertMassUsingSharedBaseUnit() {
        BigNumber result = converter.convert(new BigNumber("1000", Locale.US), Unit.MASS.GRAM, Unit.MASS.POUND);

        assertEquals(new BigDecimal("2.2046226218"), result.toBigDecimal().setScale(10, RoundingMode.HALF_UP));
    }

    @Test
    void shouldConvertTemperatureWithOffset() {
        BigNumber result = converter.convert(new BigNumber("0", Locale.US), Unit.TEMPERATURE.CELSIUS, Unit.TEMPERATURE.FAHRENHEIT);

        assertEquals(new BigDecimal("32.0000"), result.toBigDecimal().setScale(4, RoundingMode.HALF_UP));
    }

    @Test
    void shouldFailOnCategoryMismatch() {
        assertThrows(UnitConversionException.class,
                () -> converter.convert(new BigNumber("1", Locale.US), Unit.LENGTH.METER, Unit.MASS.KILOGRAM));
    }

}
