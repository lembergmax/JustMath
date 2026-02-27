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
 * Contract-style tests for the {@link ConversionFormula} abstraction.
 *
 * <p>
 * Since {@link ConversionFormula} is an interface, we validate the expected contract through
 * a known implementation ({@link AffineConversionFormula}) and a small custom stub.
 * </p>
 */
final class ConversionFormulaTest {

    /**
     * Math context used for contract tests.
     */
    private static final MathContext MC = new MathContext(34);

    /**
     * Ensures a {@link ConversionFormula} can be used as a strategy without knowing its concrete type.
     */
    @Test
    void canUseFormulaPolymorphically() {
        final ConversionFormula formula = new AffineConversionFormula(new BigNumber("10"), new BigNumber("0"));

        final BigNumber base = formula.toBase(new BigNumber("2"), MC);
        final BigNumber value = formula.fromBase(base, MC);

        assertEquals(0, value.toBigDecimal().compareTo(new java.math.BigDecimal("2")));
    }

    /**
     * Ensures the provided {@link MathContext} is passed to formula methods by callers.
     *
     * <p>
     * This test uses a stub formula that records the last received {@link MathContext}.
     * </p>
     */
    @Test
    void callersCanProvideMathContext() {
        final RecordingFormula formula = new RecordingFormula();

        formula.toBase(new BigNumber("1"), MC);
        assertSame(MC, formula.lastMathContext);

        formula.fromBase(new BigNumber("1"), MC);
        assertSame(MC, formula.lastMathContext);
    }

    /**
     * Simple stub formula that records the last received math context.
     */
    private static final class RecordingFormula implements ConversionFormula {

        /**
         * Stores the last {@link MathContext} received by either method.
         */
        private MathContext lastMathContext;

        /**
         * Records the math context and returns the input unchanged.
         *
         * @param value value to convert
         * @param mathContext math context to record
         * @return the input value unchanged
         */
        @Override
        public BigNumber toBase(final BigNumber value, final MathContext mathContext) {
            this.lastMathContext = mathContext;
            return value;
        }

        /**
         * Records the math context and returns the input unchanged.
         *
         * @param baseValue base value to convert
         * @param mathContext math context to record
         * @return the input value unchanged
         */
        @Override
        public BigNumber fromBase(final BigNumber baseValue, final MathContext mathContext) {
            this.lastMathContext = mathContext;
            return baseValue;
        }
    }

}