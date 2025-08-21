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
    private final Locale locale;
    @NonNull
    private BigNumber x;
    @NonNull
    private BigNumber y;

    /**
     * Constructs a {@code BigNumberCoordinate} at the origin (0, 0) in Cartesian coordinates
     * using the default system {@link Locale}.
     */
    public BigNumberCoordinate() {
        this(BigNumbers.ZERO, BigNumbers.ZERO, CoordinateType.CARTESIAN, Locale.getDefault());
    }

    /**
     * Constructs a {@code BigNumberCoordinate} where both x and y are set to the same value.
     * The coordinate type defaults to {@link CoordinateType#CARTESIAN}.
     *
     * @param xy the value for both x and y
     */
    public BigNumberCoordinate(BigNumber xy) {
        this(xy, xy, CoordinateType.CARTESIAN, xy.getLocale());
    }

    /**
     * Constructs a {@code BigNumberCoordinate} where both x and y are set to the same value.
     * The coordinate type defaults to {@link CoordinateType#CARTESIAN} and the given {@link Locale} is used.
     *
     * @param xy     the value for both x and y
     * @param locale the locale to use for number formatting
     */
    public BigNumberCoordinate(BigNumber xy, Locale locale) {
        this(xy, xy, CoordinateType.CARTESIAN, locale);
    }

    /**
     * Constructs a {@code BigNumberCoordinate} with the specified x and y values,
     * using {@link CoordinateType#CARTESIAN} and locale inferred from x.
     *
     * @param x the x-coordinate or radius (depending on type)
     * @param y the y-coordinate or angle (depending on type)
     */
    public BigNumberCoordinate(BigNumber x, BigNumber y) {
        this(x, y, CoordinateType.CARTESIAN, x.getLocale());
    }

    /**
     * Constructs a {@code BigNumberCoordinate} with the specified x and y values,
     * using {@link CoordinateType#CARTESIAN} and the specified locale.
     *
     * @param x      the x-coordinate or radius
     * @param y      the y-coordinate or angle
     * @param locale the locale to use for number formatting
     */
    public BigNumberCoordinate(BigNumber x, BigNumber y, Locale locale) {
        this(x, y, CoordinateType.CARTESIAN, locale);
    }

    /**
     * Constructs a {@code BigNumberCoordinate} with the specified x, y, and coordinate type.
     * The locale is inferred from x.
     *
     * @param x    the x-coordinate or radius
     * @param y    the y-coordinate or angle
     * @param type the coordinate type (CARTESIAN or POLAR)
     */
    public BigNumberCoordinate(BigNumber x, BigNumber y, CoordinateType type) {
        this(x, y, type, x.getLocale());
    }

    /**
     * Constructs a {@code BigNumberCoordinate} with the specified x, y, coordinate type, and locale.
     *
     * @param x      the x-coordinate or radius
     * @param y      the y-coordinate or angle
     * @param type   the coordinate type (CARTESIAN or POLAR)
     * @param locale the locale to use for number formatting
     */
    public BigNumberCoordinate(BigNumber x, BigNumber y, CoordinateType type, Locale locale) {
        this.type = type;
        this.locale = locale;
        this.x = new BigNumber(x, locale);
        this.y = new BigNumber(y, locale);
    }

    /**
     * Constructs a {@code BigNumberCoordinate} from two strings and a locale.
     * Useful for parsing string inputs with locale-specific number formats.
     *
     * @param xStr   the x value as string
     * @param yStr   the y value as string
     * @param type   the coordinate type (CARTESIAN or POLAR)
     * @param locale the locale to use for parsing and formatting
     */
    public BigNumberCoordinate(String xStr, String yStr, CoordinateType type, Locale locale) {
        this(new BigNumber(xStr, locale), new BigNumber(yStr, locale), type, locale);
    }

    /**
     * Constructs a {@code BigNumberCoordinate} from two string representations and a locale.
     * Both x and y values are parsed from the provided strings using the specified locale.
     * The coordinate type defaults to {@link CoordinateType#CARTESIAN}.
     *
     * @param xStr   the x value as a string
     * @param yStr   the y value as a string
     * @param locale the locale to use for parsing and formatting
     */
    public BigNumberCoordinate(String xStr, String yStr, Locale locale) {
        this(new BigNumber(xStr, locale), new BigNumber(yStr, locale), CoordinateType.CARTESIAN, locale);
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
     * Returns the string representation of this coordinate in its default format.
     * <p>
     * The output depends on the coordinate system defined by {@code type}:
     * <ul>
     *     <li>For {@code CARTESIAN}, the format is {@code "x=<value>; y=<value>"}</li>
     *     <li>For {@code POLAR}, the format is {@code "r=<value>; θ=<value>"}</li>
     * </ul>
     * No digit grouping is applied.
     *
     * @return a plain string representation of this coordinate
     */
    @Override
    public String toString() {
        return toString(x.getLocale(), false);
    }

    /**
     * Returns a human-readable string representation of this coordinate with digit grouping enabled,
     * using the {@link Locale} of the {@code x}-coordinate for number formatting.
     * <p>
     * The output depends on the coordinate system defined by {@code type}:
     * <ul>
     *     <li>For {@code CARTESIAN}, the format is {@code "x=<value>; y=<value>"}</li>
     *     <li>For {@code POLAR}, the format is {@code "r=<value>; θ=<value>"}</li>
     * </ul>
     *
     * @return a pretty-printed string representation of this coordinate
     */
    public String toPrettyString() {
        return toString(x.getLocale(), true);
    }

    /**
     * Returns a human-readable string representation of this coordinate with digit grouping enabled,
     * using the specified {@link Locale} for number formatting.
     * <p>
     * The output depends on the coordinate system defined by {@code type}:
     * <ul>
     *     <li>For {@code CARTESIAN}, the format is {@code "x=<value>; y=<value>"}</li>
     *     <li>For {@code POLAR}, the format is {@code "r=<value>; θ=<value>"}</li>
     * </ul>
     *
     * @param locale the locale to apply for number formatting
     * @return a pretty-printed string representation of this coordinate using the given locale
     */
    public String toPrettyString(@NonNull final Locale locale) {
        return toString(locale, true);
    }

    /**
     * Internal helper method that constructs the string representation of this coordinate.
     * <p>
     * Depending on the coordinate {@code type}, the method produces one of the following:
     * <ul>
     *     <li>{@code CARTESIAN}: {@code "x=<value>; y=<value>"}</li>
     *     <li>{@code POLAR}: {@code "r=<value>; θ=<value>"}</li>
     * </ul>
     * The {@code useGrouping} flag determines whether digit grouping is applied when formatting
     * the coordinate values.
     *
     * @param locale      the locale used for number formatting
     * @param useGrouping {@code true} to enable digit grouping, {@code false} for plain output
     * @return the formatted coordinate string
     */
    private String toString(@NonNull final Locale locale, boolean useGrouping) {
        String xCoordinate;
        String yCoordinate;
        if (useGrouping) {
            xCoordinate = x.trim().toPrettyString(locale);
            yCoordinate = y.trim().toPrettyString(locale);
        } else {
            xCoordinate = x.trim().toString();
            yCoordinate = y.trim().toString();
        }

        return switch (type) {
            case CARTESIAN -> "x=" + xCoordinate + "; y=" + yCoordinate;
            case POLAR -> "r=" + xCoordinate + "; θ=" + yCoordinate;
            default -> xCoordinate + "; " + yCoordinate; // not needed yet
        };
    }

}
