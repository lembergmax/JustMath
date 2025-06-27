package com.mlprograms.justmath.bignumber;

import com.mlprograms.justmath.calculator.internal.CoordinateType;
import lombok.Getter;
import lombok.NonNull;

import java.util.Locale;

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
 */
@Getter
public class BigNumberCoordinate {

	@NonNull
	private final CoordinateType type;
	@NonNull
	private BigNumber x;
	@NonNull
	private BigNumber y;

	public BigNumberCoordinate() {
		this(BigNumberValues.ZERO);
	}

	public BigNumberCoordinate(BigNumber xy) {
		this(xy, xy);
	}

	public BigNumberCoordinate(BigNumber x, BigNumber y) {
		this(x, y, CoordinateType.CARTESIAN);
	}

	public BigNumberCoordinate(BigNumber x, BigNumber y, CoordinateType type) {
		this(x, y, type, Locale.getDefault());
	}

	public BigNumberCoordinate(BigNumber x, BigNumber y, CoordinateType type, Locale locale) {
		this.x = new BigNumber(x, locale);
		this.y = new BigNumber(y, locale);
		this.type = type;
	}

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
	 * Returns a string representation of this coordinate using the object's default locale
	 * and without grouping separators.
	 *
	 * @return a basic string representation of the coordinate
	 */
	@Override
	public String toString() {
		return toString(x.getLocale(), false);
	}

	/**
	 * Returns a string representation of this coordinate using the specified {@link Locale},
	 * with grouping separators enabled.
	 *
	 * @param locale
	 * 	the locale to use for formatting
	 *
	 * @return the localized string representation with grouping
	 */
	public String toString(@NonNull final Locale locale) {
		return toString(locale, true);
	}

	/**
	 * Returns a string representation of this coordinate using the object's current locale
	 * and optional grouping separators.
	 *
	 * @param useGrouping
	 * 	whether to include grouping separators in the integer part
	 *
	 * @return the formatted string using current locale and grouping option
	 */
	public String toString(boolean useGrouping) {
		return toString(x.getLocale(), useGrouping);
	}

	/**
	 * Returns a string representation of this coordinate using the object's current locale
	 * with grouping separators enabled.
	 *
	 * @return the localized string representation with grouping
	 */
	public String toStringWithGrouping() {
		return toString(x.getLocale(), true);
	}

	/**
	 * Returns a fully formatted string representation of this coordinate using the specified
	 * {@link Locale} and grouping option.
	 *
	 * <p>Formatting is based on the coordinate type:
	 * <ul>
	 *   <li>{@code CARTESIAN}: {@code "x=<x>; y=<y>"}</li>
	 *   <li>{@code POLAR}: {@code "r=<x>; θ=<y>"}</li>
	 *   <li>Fallback: {@code "<x>, <y>"}</li>
	 * </ul>
	 * </p>
	 *
	 * @param locale
	 * 	the locale to use for formatting
	 * @param useGrouping
	 * 	whether grouping separators should be used in the integer part
	 *
	 * @return the fully formatted coordinate string
	 */
	public String toString(@NonNull final Locale locale, boolean useGrouping) {
		String xCoordinate = x.trim().toString(locale, useGrouping);
		String yCoordinate = y.trim().toString(locale, useGrouping);

		return switch (type) {
			case CARTESIAN -> "x=" + xCoordinate + "; y=" + yCoordinate;
			case POLAR -> "r=" + xCoordinate + "; θ=" + yCoordinate;
			// default -> xCoordinate + ", " + yCoordinate; // not needed yet
		};
	}

}
