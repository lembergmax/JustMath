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

import java.math.MathContext;

/**
 * Strategy interface describing how values are converted to a category base unit and back.
 * <p>
 * Every concrete unit is defined by a {@link ConversionFormula} that maps:
 * </p>
 * <ul>
 *   <li>unit value {@code ->} base unit value</li>
 *   <li>base unit value {@code ->} unit value</li>
 * </ul>
 *
 * <p>
 * Implementations must be immutable and thread-safe.
 * </p>
 */
public interface ConversionFormula {

    /**
     * Converts a value expressed in a concrete unit to the base unit of the category.
     *
     * @param value       the input value expressed in the concrete unit; must not be {@code null}
     * @return the converted value expressed in the category base unit; never {@code null}
     */
    BigNumber toBase(BigNumber value);

    /**
     * Converts a value expressed in the category base unit to the concrete unit.
     *
     * @param baseValue   the input value expressed in the base unit; must not be {@code null}
     * @return the converted value expressed in the concrete unit; never {@code null}
     */
    BigNumber fromBase(BigNumber baseValue);

}