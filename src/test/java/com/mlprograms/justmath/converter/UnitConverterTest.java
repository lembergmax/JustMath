/*
 * Copyright (c) 2026 Max Lemberg
 *
 * This file is part of JustMath.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mlprograms.justmath.converter;

import com.mlprograms.justmath.bignumber.BigNumber;
import org.junit.jupiter.api.Test;

import java.math.MathContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests for {@link UnitConverter}.
 */
final class UnitConverterTest {

    /**
     * Ensures the converter stores and exposes the configured math context.
     */
    @Test
    void exposesMathContext() {
        final MathContext mc = new MathContext(10);
        final UnitConverter converter = new UnitConverter(mc);
        assertSame(mc, converter.getMathContext());
    }

    /**
     * Ensures identity conversions keep the numeric value (within compareTo semantics).
     */
    @Test
    void identityConversionReturnsSameNumericValue() {
        final UnitConverter converter = new UnitConverter(new MathContext(34));
        final BigNumber input = new BigNumber("123.456");

        final BigNumber output = converter.convert(input, Unit.METER, Unit.METER);

        assertEquals(0, output.toBigDecimal().compareTo(input.toBigDecimal()));
    }

    /**
     * Ensures precision/rounding is applied for divisions when converting from base to a smaller-scale unit.
     *
     * <p>
     * Example: 1 meter -> inch requires division by 0.0254 and produces a repeating decimal.
     * With a small precision context, the output must be rounded accordingly.
     * </p>
     */
    @Test
    void roundingIsAppliedUsingMathContext() {
        final MathContext mc = new MathContext(5);
        final UnitConverter converter = new UnitConverter(mc);

        final BigNumber oneMeter = new BigNumber("1");
        final BigNumber inches = converter.convert(oneMeter, Unit.METER, Unit.INCH);

        final java.math.BigDecimal expected =
                new java.math.BigDecimal("1").divide(new java.math.BigDecimal("0.0254"), mc);

        assertEquals(0, inches.toBigDecimal().compareTo(expected));
    }

}