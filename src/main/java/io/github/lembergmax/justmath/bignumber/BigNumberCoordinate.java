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

import java.util.Locale;
import java.util.Objects;

import io.github.lembergmax.justmath.calculator.internal.CoordinateType;
import lombok.Getter;
import lombok.NonNull;

/**
 * An immutable data structure representing a 2D coordinate with arbitrary precision,
 * based on {@link BigNumber} components.
 * <p>
 * This type encapsulates both Cartesian and polar coordinates, depending on the {@link CoordinateType}.
 * It uses {@link BigNumber} for the coordinate values to support high-precision mathematical computations.
 * </p>
 *
 * <p>
 * Instances are immutable: {@code x} and {@code y} are assigned once at construction and the
 * {@link #trim()} method returns a new, trimmed coordinate instead of mutating the receiver. This makes
 * {@code BigNumberCoordinate} safe to use as a key in hash-based collections. Equality and ordering are
 * defined over the full {@code (type, x, y)} triple, not just the inherited {@code x} value (audit K1).
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
public class BigNumberCoordinate extends BigNumber implements MultiValueResult {

    /**
     * Type of this coordinate. Determines how {@code x} and {@code y} are interpreted:
     * - {@code CARTESIAN}: {@code x} and {@code y} are Cartesian coordinates.
     * - {@code POLAR}: {@code x} is radius (r) and {@code y} is angle (θ).
     */
    @NonNull
    private final CoordinateType type;

    /**
     * Locale used for parsing and formatting the numeric {@link BigNumber} components.
     * This locale is applied when producing localized string representations.
     */
    @NonNull
    private final Locale locale;

    /**
     * First component of the coordinate. In CARTESIAN mode this is the X coordinate;
     * in POLAR mode this represents the radius (r).
     */
    @NonNull
    private final BigNumber x;

    /**
     * Second component of the coordinate. In CARTESIAN mode this is the Y coordinate;
     * in POLAR mode this represents the angle (θ), typically in radians.
     */
    @NonNull
    private final BigNumber y;

    /**
     * Constructs a {@code BigNumberCoordinate} at the origin (0, 0) in Cartesian coordinates
     * using {@link Locale#US}. The previous JVM-default locale was deliberately replaced to
     * keep the behaviour of test suites and library callers deterministic across machines.
     */
    public BigNumberCoordinate() {
        this(BigNumbers.ZERO, BigNumbers.ZERO, CoordinateType.CARTESIAN, Locale.US);
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
        super(new BigNumber(x, locale));
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
     * Returns a new {@code BigNumberCoordinate} with insignificant leading and trailing zeros removed
     * from both components (leading zeros before the decimal point, trailing zeros after it).
     * <p>
     * The receiver is not modified. Consistent with the project naming convention, the absence of a
     * {@code *This} suffix signals that a fresh instance is returned rather than the receiver mutated.
     *
     * @return a new, trimmed {@code BigNumberCoordinate} preserving this coordinate's type and locale
     */
    public BigNumberCoordinate trim() {
        return new BigNumberCoordinate(x.clone().trim(), y.clone().trim(), type, locale);
    }

    /**
     * Returns the first scalar component of this coordinate as a plain
     * {@link BigNumber}. For {@link CoordinateType#POLAR} this is the radius
     * {@code r}; for {@link CoordinateType#CARTESIAN} it is the {@code x}
     * coordinate. The returned instance is a fresh {@code BigNumber} (not a
     * {@code BigNumberCoordinate}) so it behaves as a normal scalar in
     * downstream arithmetic and formatting.
     *
     * @return the first scalar component; never {@code null}
     */
    @Override
    public BigNumber firstValue() {
        return new BigNumber(x, locale);
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
     * Returns the plain string representation of this coordinate using the given {@link Locale}
     * for both components. No digit grouping is applied.
     * <p>
     * Both the {@code x} and {@code y} components are formatted with the supplied locale's
     * decimal separator, so the full pair is returned in a locale-aware form
     * (e.g. {@code "x=1,5; y=2,5"} for {@link Locale#GERMANY}).
     *
     * @param locale the locale to apply for number formatting; must not be {@code null}
     * @return a plain string representation of this coordinate using the given locale
     */
    @Override
    public String toString(@NonNull final Locale locale) {
        return toString(locale, false);
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
     * The {@code prettyString} flag determines whether digit grouping is applied when formatting
     * the coordinate values.
     *
     * @param locale       the locale used for number formatting
     * @param prettyString {@code true} to enable digit grouping, {@code false} for plain output
     * @return the formatted coordinate string
     */
    private String toString(@NonNull final Locale locale, boolean prettyString) {
        String xCoordinate;
        String yCoordinate;

        if (prettyString) {
            xCoordinate = x.toPrettyString(locale);
            yCoordinate = y.toPrettyString(locale);
        } else {
            xCoordinate = x.toString(locale);
            yCoordinate = y.toString(locale);
        }

        return switch (type) {
            case CARTESIAN -> "x=" + xCoordinate + "; y=" + yCoordinate;
            case POLAR -> "r=" + xCoordinate + "; θ=" + yCoordinate;
        };
    }

    /**
     * Equality over the full coordinate triple {@code (type, x, y)} using numeric component equality.
     * <p>
     * The inherited {@link BigNumber#equals(Object)} only considers the first component and would treat
     * {@code (1,2)} and {@code (1,5)}, or a Cartesian and a polar pair with identical numbers, as equal —
     * silently corrupting hash-based and sorted collections. A coordinate is never equal to a plain
     * {@link BigNumber}; {@link BigNumber#equals(Object)} enforces the mirror image so equality stays
     * symmetric (audit K1).
     *
     * @param other the object to compare with
     * @return {@code true} only if {@code other} is a {@code BigNumberCoordinate} with the same type and
     * numerically equal {@code x} and {@code y}
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BigNumberCoordinate otherCoordinate)) {
            return false;
        }
        return type == otherCoordinate.type
                && x.equals(otherCoordinate.x)
                && y.equals(otherCoordinate.y);
    }

    /**
     * Hash code consistent with {@link #equals(Object)}: derived from {@code type} and the numeric hashes
     * of {@code x} and {@code y}, so coordinates that differ only in representation (for example
     * {@code "1.0"} vs {@code "1.00"}) share a bucket.
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, x, y);
    }

    /**
     * Total ordering consistent with {@link #equals(Object)} (contract {@code (a.compareTo(b) == 0) ⇔
     * a.equals(b)}). Coordinates are ordered lexicographically by {@code (type, x, y)}; any coordinate
     * sorts after any plain {@link BigNumber}, mirrored by {@link BigNumber#compareTo(BigNumber)} so the
     * mixed comparison is antisymmetric and never reports equality (audit K1).
     *
     * @param other the value to compare with; must not be {@code null}
     * @return a negative integer, zero, or a positive integer as this coordinate is less than, equal to,
     * or greater than {@code other}
     */
    @Override
    public int compareTo(@NonNull final BigNumber other) {
        if (!(other instanceof BigNumberCoordinate otherCoordinate)) {
            return 1;
        }
        final int typeComparison = Integer.compare(type.ordinal(), otherCoordinate.type.ordinal());
        if (typeComparison != 0) {
            return typeComparison;
        }
        final int xComparison = x.compareTo(otherCoordinate.x);
        if (xComparison != 0) {
            return xComparison;
        }
        return y.compareTo(otherCoordinate.y);
    }

}
