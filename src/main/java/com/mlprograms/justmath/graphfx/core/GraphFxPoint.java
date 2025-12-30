package com.mlprograms.justmath.graphfx.core;

/**
 * Immutable 2D point in world coordinates.
 *
 * <p>This type is UI-framework agnostic and is used by GraphFx core computations.</p>
 *
 * <h2>Thread-safety</h2>
 * <p>Immutable and therefore thread-safe.</p>
 *
 * @param x x coordinate in world units
 * @param y y coordinate in world units
 */
public record GraphFxPoint(double x, double y) {
}
