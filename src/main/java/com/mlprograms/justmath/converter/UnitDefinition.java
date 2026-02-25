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
import lombok.NonNull;

import java.math.MathContext;

/**
 * Immutable definition of a unit, containing human-readable metadata and conversion behavior.
 * <p>
 * This type is intentionally separated from {@link Unit} to keep enums as pure identifiers.
 * </p>
 *
 * @param category     the category of the unit; must not be {@code null}
 * @param displayName  human-readable name intended for UI display; must not be {@code null}
 * @param symbol       short unit symbol used for parsing/formatting; must not be {@code null}
 * @param formula      conversion behavior to and from the base unit of the category; must not be {@code null}
 */
record UnitDefinition(
        @NonNull UnitCategory category,
        @NonNull String displayName,
        @NonNull String symbol,
        @NonNull ConversionFormula formula
) {

    /**
     * Converts a value from the concrete unit into the base unit of its category.
     *
     * @param value       the value expressed in the concrete unit; must not be {@code null}
     * @param mathContext math context controlling precision/rounding; must not be {@code null}
     * @return the converted value expressed in the category base unit; never {@code null}
     */
    BigNumber toBase(@NonNull final BigNumber value, @NonNull final MathContext mathContext) {
        return formula.toBase(value, mathContext);
    }

    /**
     * Converts a value from the category base unit into the concrete unit.
     *
     * @param baseValue   the value expressed in the base unit; must not be {@code null}
     * @param mathContext math context controlling precision/rounding; must not be {@code null}
     * @return the converted value expressed in the concrete unit; never {@code null}
     */
    BigNumber fromBase(@NonNull final BigNumber baseValue, @NonNull final MathContext mathContext) {
        return formula.fromBase(baseValue, mathContext);
    }

}