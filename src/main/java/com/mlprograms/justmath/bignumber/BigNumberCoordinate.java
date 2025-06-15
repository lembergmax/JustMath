package com.mlprograms.justmath.bignumber;

import com.mlprograms.justmath.calculator.internal.CoordinateType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * An immutable data structure representing a 2D coordinate with arbitrary precision,
 * based on {@link BigNumber} components.
 * <p>
 * This record encapsulates both Cartesian and polar coordinates, depending on the {@link CoordinateType}.
 * It uses {@link BigNumber} for the coordinate values to support high-precision mathematical computations.
 * </p>
 *
 * <ul>
 *   <li>If the {@code type} is {@code CARTESIAN}, then:
 *     <ul>
 *       <li>{@code x} represents the horizontal axis (x-coordinate)</li>
 *       <li>{@code y} represents the vertical axis (y-coordinate)</li>
 *     </ul>
 *   </li>
 *   <li>If the {@code type} is {@code POLAR}, then:
 *     <ul>
 *       <li>{@code x} represents the radius (r)</li>
 *       <li>{@code y} represents the angle (θ), typically in radians</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p>
 * This record is often used for conversion between coordinate systems and for evaluating
 * functions that return 2D results, such as {@code atan2}, polar-to-Cartesian transformations,
 * or complex number representations.
 * </p>
 *
 * @param x
 * 	the first coordinate component (x or r); must not be {@code null}
 * @param y
 * 	the second coordinate component (y or θ); must not be {@code null}
 * @param type
 * 	the coordinate type indicating the meaning of {@code x} and {@code y}; must not be {@code null}
 */
@Getter
@AllArgsConstructor
public class BigNumberCoordinate {

	@NonNull
	private BigNumber x;

	@NonNull
	private BigNumber y;

	@NonNull
	private CoordinateType type;

	/**
	 * Removes insignificant leading and trailing zeros from the {@link BigNumber} x and y representation.
	 * This includes leading zeros before the decimal point and trailing zeros after the decimal point.
	 *
	 * @return this {@code BigNumber} instance with trimmed parts
	 */
	public BigNumberCoordinate trim() {
		x = x.trim();
		y = y.trim();
		return this;
	}

	/**
	 * Returns a string representation of this coordinate, formatted according to its type.
	 * <ul>
	 *   <li>If the type is {@code CARTESIAN}, returns "x=<x>; y=<y>".</li>
	 *   <li>If the type is {@code POLAR}, returns "r=<x>; θ=<y>".</li>
	 *   <li>Otherwise, returns "<x>, <y>".</li>
	 * </ul>
	 * The values are trimmed to remove insignificant zeros.
	 *
	 * @return a string representation of this coordinate
	 */
	@Override
	public String toString() {
		String xCoordinate = x.trim().toString();
		String yCoordinate = y.trim().toString();

		if (type == CoordinateType.CARTESIAN) {
			return "x=" + xCoordinate + "; y=" + yCoordinate;
		} else if (type == CoordinateType.POLAR) {
			return "r=" + xCoordinate + "; θ=" + yCoordinate;
		} else {
			return xCoordinate + ", " + yCoordinate; // Fallback
		}
	}

}
