/*
 * Copyright (c) 2025-2026 Max Lemberg
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

package io.github.lembergmax.justmath.bignumber;

/**
 * Marker for evaluator results that carry more than one scalar component
 * (for example the {@code (r, θ)} pair returned by {@code Pol(...)} or the
 * {@code (x, y)} pair returned by {@code Rec(...)}).
 *
 * <p>When such a value participates in a larger scalar expression — for
 * instance as an operand of {@code +}, {@code -}, {@code *}, {@code /},
 * {@code ^}, or as an argument to a function that expects a single number —
 * it is automatically coerced to its {@link #firstValue() first component}.
 * Implementations decide what "first" means for their domain
 * (e.g. {@code r} for polar, {@code x} for cartesian).</p>
 */
public interface MultiValueResult {

    /**
     * Returns the first scalar component of this multi-value result as a
     * plain {@link BigNumber}. The returned value must not itself be a
     * multi-value result.
     *
     * @return the first scalar component; never {@code null}
     */
    BigNumber firstValue();

}
