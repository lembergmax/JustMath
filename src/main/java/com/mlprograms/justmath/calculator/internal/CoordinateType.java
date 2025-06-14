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

