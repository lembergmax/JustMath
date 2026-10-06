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
/**
 * Arbitrary-precision numbers and the collections built on them.
 *
 * <p>{@link io.github.lembergmax.justmath.bignumber.BigNumber} is a locale-aware decimal number without a
 * size limit. {@link io.github.lembergmax.justmath.bignumber.BigNumberList} and
 * {@link io.github.lembergmax.justmath.bignumber.BigNumberMatrix} add statistics, sorting and linear algebra,
 * and {@link io.github.lembergmax.justmath.bignumber.BigNumberCoordinate} carries the two components of a
 * polar or cartesian point.</p>
 *
 * <p>Operation methods such as {@code add}, {@code sin} or {@code round} never change the receiver and
 * return a new instance. The few methods that change the receiver in place end in {@code This}
 * (for example {@code negateThis()}) or are setters. The constants in
 * {@link io.github.lembergmax.justmath.bignumber.BigNumbers} are shared, so results are never handed out as
 * the constant itself.</p>
 *
 * <p>The locale of a number controls how it is formatted and which separators its string form uses. It has
 * no influence on arithmetic, comparison or hashing.</p>
 */
package io.github.lembergmax.justmath.bignumber;
