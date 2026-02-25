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
import com.mlprograms.justmath.calculator.CalculatorEngineUtils;
import com.mlprograms.justmath.converter.exception.UnitConversionException;
import lombok.Getter;
import lombok.NonNull;

import java.math.MathContext;

import static com.mlprograms.justmath.bignumber.BigNumbers.DEFAULT_DIVISION_PRECISION;

/**
 * Converts numeric values between units belonging to the same unit group.
 *
 * <p>
 * A "unit group" corresponds to the nested enum type, such as {@link Unit.Length} ...
 * Cross-group conversions are rejected.
 * </p>
 *
 * <p>
 * Instances are immutable and thread-safe.
 * </p>
 */
public final class UnitConverter {

    /**
     * Math context used to control precision and rounding behavior for conversions.
     */
    @Getter
    private final MathContext mathContext;

    /**
     * Creates a converter using the library's default division precision.
     */
    public UnitConverter() {
        this(DEFAULT_DIVISION_PRECISION);
    }

    /**
     * Creates a converter using a math context derived from the provided precision.
     *
     * @param divisionPrecision precision used to build the internal {@link MathContext}
     */
    public UnitConverter(final int divisionPrecision) {
        this(CalculatorEngineUtils.getDefaultMathContext(divisionPrecision));
    }

    /**
     * Creates a converter with an explicit math context.
     *
     * @param mathContext math context controlling precision and rounding; must not be {@code null}
     */
    public UnitConverter(@NonNull final MathContext mathContext) {
        this.mathContext = mathContext;
    }

    /**
     * Converts {@code value} from {@code fromUnit} to {@code toUnit}.
     *
     * <p>
     * Conversions are only valid within the same unit group. The group is encoded by the unit's runtime type:
     * </p>
     * <ul>
     *   <li>{@link Unit.Length} units can convert to other {@link Unit.Length} units</li>
     *   ...
     * </ul>
     *
     * @param value input value; must not be {@code null}
     * @param fromUnit source unit; must not be {@code null}
     * @param toUnit target unit; must not be {@code null}
     * @return converted value; never {@code null}
     * @throws UnitConversionException if the units belong to different groups
     */
    public BigNumber convert(@NonNull final BigNumber value, @NonNull final Unit fromUnit, @NonNull final Unit toUnit) {
        if (!UnitElements.areCompatible(fromUnit, toUnit)) {
            throw new UnitConversionException(
                    "Incompatible unit groups: cannot convert from " + fromUnit + " (" + fromUnit.getClass().getSimpleName() + ") to "
                            + toUnit + " (" + toUnit.getClass().getSimpleName() + ")."
            );
        }

        final BigNumber base = UnitElements.toBase(fromUnit, value, mathContext);
        return UnitElements.fromBase(toUnit, base, mathContext);
    }

}