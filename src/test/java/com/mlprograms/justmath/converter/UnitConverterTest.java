package com.mlprograms.justmath.converter;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.converter.unit.Unit;
import org.junit.jupiter.api.Test;

import java.math.MathContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UnitConverterTest {

    private final UnitConverter converter = new UnitConverter(MathContext.DECIMAL64);

    @Test
    void shouldConvertLengthWithCentralBaseRule() {
        BigNumber result = converter.convert(new BigNumber("1"), Unit.METER, Unit.CENTIMETER);

        assertEquals(new BigNumber("100"), result);
    }

    @Test
    void shouldConvertTemperatureWithAffineRule() {
        BigNumber result = converter.convert(new BigNumber("100"), Unit.CELSIUS, Unit.FAHRENHEIT);

        assertEquals(new BigNumber("212"), result.round(8));
    }

    @Test
    void shouldThrowForCategoryMismatch() {
        assertThrows(ConversionException.class,
                () -> converter.convert(new BigNumber("1"), Unit.METER, Unit.KILOGRAM));
    }

}
