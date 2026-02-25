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
 * Strategy interface that defines how a concrete unit value is converted
 * to the base unit of a category and back.
 * <p>
 * A {@link ConversionFormula} is the conversion "engine" for a unit definition.
 * It must be:
 * </p>
 * <ul>
 *   <li><strong>Immutable</strong> (no mutable state)</li>
 *   <li><strong>Thread-safe</strong> (safe to reuse across threads)</li>
 *   <li><strong>Deterministic</strong> (same input produces same output)</li>
 * </ul>
 *
 * <p>
 * The converter module uses {@link MathContext} to control precision and rounding.
 * Even if an implementation does not need the {@link MathContext} for some operations
 * (e.g., multiplication), it must accept it to keep a stable contract and to allow
 * consistent use across different formulas.
 * </p>
 */
public interface ConversionFormula {

    /**
     * Converts a value expressed in a concrete unit into the base unit of the unit's category.
     *
     * <p>
     * Example (length category, base = meter):
     * converting {@code 1 km} to base might produce {@code 1000 m}.
     * </p>
     *
     * @param value       the value expressed in the concrete unit; must not be {@code null}
     * @param mathContext the math context controlling precision and rounding; must not be {@code null}
     * @return the corresponding value expressed in the category base unit; never {@code null}
     */
    BigNumber toBase(@NonNull BigNumber value, @NonNull MathContext mathContext);

    /**
     * Converts a value expressed in the base unit of the unit's category back into the concrete unit.
     *
     * <p>
     * Example (length category, base = meter):
     * converting {@code 1000 m} from base might produce {@code 1 km}.
     * </p>
     *
     * @param baseValue   the value expressed in the category base unit; must not be {@code null}
     * @param mathContext the math context controlling precision and rounding; must not be {@code null}
     * @return the corresponding value expressed in the concrete unit; never {@code null}
     */
    BigNumber fromBase(@NonNull BigNumber baseValue, @NonNull MathContext mathContext);

}