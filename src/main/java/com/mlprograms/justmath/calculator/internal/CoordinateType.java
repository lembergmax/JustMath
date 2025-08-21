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

package com.mlprograms.justmath.calculator.internal;

/**
 * Enum representing the type of 2D coordinate system.
 * <p>
 * This enum distinguishes between Cartesian and Polar coordinate systems,
 * which determine how coordinate values are interpreted.
 * </p>
 *
 * <ul>
 *   <li>{@link #CARTESIAN} - Represents a Cartesian coordinate system where
 *       the two values correspond to horizontal (x) and vertical (y) components.</li>
 *   <li>{@link #POLAR} - Represents a Polar coordinate system where
 *       the two values correspond to radius (r) and angle (θ, typically in radians).</li>
 * </ul>
 * <p>
 * This enum is typically used in conjunction with {@code BigNumberCoordinateRecord}
 * to clarify the meaning of coordinate components.
 */
public enum CoordinateType {

	CARTESIAN, POLAR

}

