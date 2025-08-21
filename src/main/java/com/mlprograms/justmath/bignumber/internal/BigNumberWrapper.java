/*
 * Copyright (c) 2025 Max Lemberg
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

package com.mlprograms.justmath.bignumber.internal;

import com.mlprograms.justmath.bignumber.BigNumber;

/**
 * A special subclass of {@link BigNumber} used to represent coordinate-based results
 * (e.g., polar or Cartesian coordinates) as formatted strings.
 * <p>
 * This class wraps a human-readable coordinate string (such as {@code "x=1.0; y=2.0"} or
 * {@code "r=5.0; θ=1.57"}) inside a {@code BigNumber} instance, allowing it to be used
 * seamlessly in contexts where a {@code BigNumber} is expected. However, this instance
 * does not represent an actual numeric value for computation.
 * </p>
 * <p>
 * Internally, the constructor calls the {@code BigNumber} superclass with a dummy value ("0"),
 * since the actual numeric value is not used. The main purpose is string formatting and
 * consistent typing across the application.
 * </p>
 */
public class BigNumberWrapper extends BigNumber {

	/**
	 * The formatted coordinate string (e.g., "x=1.0; y=2.0" or "r=3.0; θ=1.57").
	 */
	private final String formattedString;

	/**
	 * Constructs a {@code BigNumberCoordinate} with the given formatted string.
	 * <p>
	 * The string should represent a coordinate in human-readable form, such as
	 * "x=...; y=..." or "r=...; θ=...".
	 * </p>
	 *
	 * @param formattedString
	 * 	the formatted coordinate string to wrap
	 */
	public BigNumberWrapper(String formattedString) {
		super("0"); // Dummy value for superclass
		this.formattedString = formattedString;
	}

	/**
	 * Returns the formatted coordinate string.
	 *
	 * @return the coordinate string (e.g., "x=1.0; y=2.0")
	 */
	@Override
	public String toString() {
		return formattedString;
	}

}
