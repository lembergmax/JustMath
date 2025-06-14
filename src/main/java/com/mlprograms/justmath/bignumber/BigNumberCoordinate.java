package com.mlprograms.justmath.bignumber;

import com.mlprograms.justmath.calculator.internal.CoordinateType;
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
public record BigNumberCoordinate(@NonNull BigNumber x, @NonNull BigNumber y, @NonNull CoordinateType type) {

}
