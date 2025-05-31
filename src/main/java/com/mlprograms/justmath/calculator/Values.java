package com.mlprograms.justmath.calculator;

import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Constants.java
 * <p>
 * This class defines global constants used throughout the exact calculator engine.
 * It provides configuration for numerical precision, trigonometric mode, and other shared values.
 */
public final class Values {

	/**
	 * Constant for pi.
	 */
	public static final String PI = "3.14159265358979323846264338327950288419716939937510";
	/**
	 * Constant for Euler's number.
	 */
	public static final String E = "2.71828182845904523536028747135266249775724709369995";
	/**
	 * Trigonometric mode: "Deg" for degrees, "Rad" for radians.
	 * This can be switched globally by changing the constant value.
	 */
	public static TrigonometricMode MODE = TrigonometricMode.DEG;
	/**
	 * Global mathematical precision for internal BigDecimal operations.
	 * Set high enough to ensure exact intermediate calculations.
	 */
	public static MathContext MATH_CONTEXT = new MathContext(10000, RoundingMode.HALF_UP);

}
