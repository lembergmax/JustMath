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
 * A "unit group" corresponds to the nested enum type, such as {@link Unit.Length}, {@link Unit.Mass},
 * {@link Unit.Area} or {@link Unit.Temperature}. Cross-group conversions are rejected.
 * </p>
 *
 * <h2>Return type design</h2>
 * <p>
 * This converter returns a {@link UnitValue} by default to keep the result self-describing:
 * it always contains both the converted {@link BigNumber} and its target {@link Unit}.
 * </p>
 *
 * <p>
 * If you only need the numeric value, use the explicit {@code convertToBigNumber(...)} methods.
 * </p>
 *
 * <h2>Thread-safety</h2>
 * <p>
 * Instances are immutable and thread-safe as long as {@link MathContext} and dependent components are treated
 * as immutable (which they are in standard Java usage).
 * </p>
 */
public final class UnitConverter {

    /**
     * Math context used to control precision and rounding behavior for conversions.
     *
     * <p>
     * This math context is applied to operations that require rounding (e.g. division in inverse affine formulas).
     * </p>
     */
    @Getter
    private final MathContext mathContext;

    /**
     * Creates a converter using the library's default division precision.
     *
     * <p>
     * The precision is derived from {@link com.mlprograms.justmath.bignumber.BigNumbers#DEFAULT_DIVISION_PRECISION} via
     * {@link CalculatorEngineUtils#getDefaultMathContext(int)}.
     * </p>
     */
    public UnitConverter() {
        this(DEFAULT_DIVISION_PRECISION);
    }

    /**
     * Creates a converter using a {@link MathContext} derived from the provided division precision.
     *
     * <p>
     * This constructor is a convenience overload for callers that want "a different default precision"
     * without manually creating a {@link MathContext}.
     * </p>
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
     * @throws NullPointerException if {@code mathContext} is {@code null}
     */
    public UnitConverter(@NonNull final MathContext mathContext) {
        this.mathContext = mathContext;
    }

    /**
     * Converts {@code value} from {@code fromUnit} to {@code toUnit} and returns a {@link UnitValue}.
     *
     * <p>
     * This overload parses the numeric input using {@link BigNumber#BigNumber(String)}.
     * For locale-aware parsing and input like {@code "1.234,56"}, use {@link UnitValue} parsing constructors
     * and then {@link #convert(UnitValue, Unit)}.
     * </p>
     *
     * @param value    numeric value as text; must not be {@code null}
     * @param fromUnit source unit; must not be {@code null}
     * @param toUnit   target unit; must not be {@code null}
     * @return converted value wrapped as {@link UnitValue} using {@code toUnit}; never {@code null}
     * @throws UnitConversionException if the units belong to different groups
     * @throws NullPointerException    if any argument is {@code null}
     */
    public UnitValue convert(@NonNull final String value, @NonNull final Unit fromUnit, @NonNull final Unit toUnit) {
        return convert(new BigNumber(value), fromUnit, toUnit);
    }

    /**
     * Converts {@code value} from {@code fromUnit} to {@code toUnit} and returns a {@link UnitValue}.
     *
     * <p>
     * Conversions are only valid within the same unit group. The group is encoded by the unit's runtime type:
     * </p>
     * <ul>
     *   <li>{@link Unit.Length} units can convert to other {@link Unit.Length} units</li>
     *   <li>{@link Unit.Mass} units can convert to other {@link Unit.Mass} units</li>
     *   <li>{@link Unit.Area} units can convert to other {@link Unit.Area} units</li>
     *   <li>{@link Unit.Temperature} units can convert to other {@link Unit.Temperature} units</li>
     * </ul>
     *
     * @param value    input numeric value expressed in {@code fromUnit}; must not be {@code null}
     * @param fromUnit source unit; must not be {@code null}
     * @param toUnit   target unit; must not be {@code null}
     * @return converted value wrapped as {@link UnitValue} using {@code toUnit}; never {@code null}
     * @throws UnitConversionException if the units belong to different groups
     * @throws NullPointerException    if any argument is {@code null}
     */
    public UnitValue convert(@NonNull final BigNumber value, @NonNull final Unit fromUnit, @NonNull final Unit toUnit) {
        final BigNumber converted = convertToBigNumber(value, fromUnit, toUnit);
        return new UnitValue(converted, toUnit);
    }

    /**
     * Converts a {@link UnitValue} to {@code toUnit} and returns a new {@link UnitValue}.
     *
     * <p>
     * This is the most convenient API when you already have a parsed value such as {@code new UnitValue("12.5 km")}.
     * </p>
     *
     * @param input  input value; must not be {@code null}
     * @param toUnit target unit; must not be {@code null}
     * @return converted value wrapped as {@link UnitValue} using {@code toUnit}; never {@code null}
     * @throws UnitConversionException if the units belong to different groups
     * @throws NullPointerException    if any argument is {@code null}
     */
    public UnitValue convert(@NonNull final UnitValue input, @NonNull final Unit toUnit) {
        return convert(input.getValue(), input.getUnit(), toUnit);
    }

    /**
     * Converts {@code value} from {@code fromUnit} to {@code toUnit} and returns only the numeric result.
     *
     * <p>
     * This method exists for call sites that do not want a {@link UnitValue} wrapper.
     * For most APIs, prefer {@link #convert(BigNumber, Unit, Unit)} which returns {@link UnitValue}.
     * </p>
     *
     * @param value    input numeric value expressed in {@code fromUnit}; must not be {@code null}
     * @param fromUnit source unit; must not be {@code null}
     * @param toUnit   target unit; must not be {@code null}
     * @return converted numeric value; never {@code null}
     * @throws UnitConversionException if the units belong to different groups
     * @throws NullPointerException    if any argument is {@code null}
     */
    public BigNumber convertToBigNumber(
            @NonNull final BigNumber value,
            @NonNull final Unit fromUnit,
            @NonNull final Unit toUnit
    ) {
        if (!UnitElements.areCompatible(fromUnit, toUnit)) {
            throw new UnitConversionException(
                    "Incompatible unit groups: cannot convert from " + fromUnit + " (" + fromUnit.getClass().getSimpleName() + ") to "
                            + toUnit + " (" + toUnit.getClass().getSimpleName() + ")."
            );
        }

        final BigNumber base = UnitElements.toBase(fromUnit, value, mathContext);
        return UnitElements.fromBase(toUnit, base, mathContext);
    }

    /**
     * Converts a {@link UnitValue} to {@code toUnit} and returns only the numeric result.
     *
     * <p>
     * This method is useful when you already have a {@link UnitValue} but need a plain number
     * (e.g. for internal calculations or formatting).
     * </p>
     *
     * @param input  input value; must not be {@code null}
     * @param toUnit target unit; must not be {@code null}
     * @return converted numeric value; never {@code null}
     * @throws UnitConversionException if the units belong to different groups
     * @throws NullPointerException    if any argument is {@code null}
     */
    public BigNumber convertToBigNumber(@NonNull final UnitValue input, @NonNull final Unit toUnit) {
        return convertToBigNumber(input.getValue(), input.getUnit(), toUnit);
    }

    /**
     * Converts {@code value} from {@code fromUnit} to {@code toUnit} and returns only the numeric result.
     *
     * <p>
     * This method exists for call sites that do not want a {@link UnitValue} wrapper.
     * For most APIs, prefer {@link #convert(BigNumber, Unit, Unit)} which returns {@link UnitValue}.
     * </p>
     *
     * @param value    input numeric value expressed in {@code fromUnit}; must not be {@code null}
     * @param fromUnit source unit; must not be {@code null}
     * @param toUnit   target unit; must not be {@code null}
     * @return converted numeric value; never {@code null}
     * @throws UnitConversionException if the units belong to different groups
     * @throws NullPointerException    if any argument is {@code null}
     */
    public BigNumber convertToBigNumber(
            @NonNull final String value,
            @NonNull final Unit fromUnit,
            @NonNull final Unit toUnit
    ) {
        return convertToBigNumber(new BigNumber(value), fromUnit, toUnit);
    }

}