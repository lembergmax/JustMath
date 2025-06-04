package com.mlprograms.justmath.calculator;

import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Utility class for advanced mathematical functions using BigDecimal.
 * Provides high-precision implementations of pow, sqrt, factorial, log, sin, cos, etc.
 */
@AllArgsConstructor
public class MathFunctions {

	/**
	 * Constant representing the value 2 as BigDecimal.
	 */
	private final BigDecimal TWO = new BigDecimal("2");
	/**
	 * Constant representing the value 3 as BigDecimal.
	 */
	private final BigDecimal THREE = new BigDecimal("3");
	/**
	 * Reference to the CalculatorEngine used for advanced calculations.
	 */
	private final MathContext mathContext;
	private final TrigonometricMode trigonometricMode;

	/**
	 * Computes base raised to the power of exponent.
	 *
	 * @param base
	 * 	base number
	 * @param exponent
	 * 	exponent (can be fractional)
	 *
	 * @return base ^ exponent
	 */
	public BigDecimal pow(BigDecimal base, BigDecimal exponent) {
		if (exponent.scale() == 0) {
			return base.pow(exponent.intValueExact());
		}
		double result = Math.pow(base.doubleValue(), exponent.doubleValue());
		return new BigDecimal(result);
	}

	/**
	 * Computes square root using Newton-Raphson iteration.
	 *
	 * @param value
	 * 	input value
	 *
	 * @return √value
	 */
	public BigDecimal sqrt(BigDecimal value) {
		if (value.compareTo(BigDecimal.ZERO) < 0)
			throw new ArithmeticException("Square root of negative number");

		BigDecimal x = new BigDecimal(Math.sqrt(value.doubleValue()), mathContext);
		BigDecimal prev;

		do {
			prev = x;
			x = x.add(value.divide(x, mathContext)).divide(TWO, mathContext);
		} while (!x.equals(prev));

		return x;
	}

	/**
	 * Computes cube root using Newton-Raphson method.
	 *
	 * @param value
	 * 	input value
	 *
	 * @return ∛value
	 */
	public BigDecimal cbrt(BigDecimal value) {
		BigDecimal x = new BigDecimal(Math.cbrt(value.doubleValue()), mathContext);
		BigDecimal prev;
		do {
			prev = x;
			x = TWO.multiply(x).add(value.divide(x.multiply(x), mathContext))
				    .divide(THREE, mathContext);
		} while (!x.equals(prev));

		return x;
	}

	/**
	 * Computes factorial (only for non-negative integers ≤ 1000).
	 *
	 * @param n
	 * 	input number
	 *
	 * @return n!
	 */
	public BigDecimal factorial(BigDecimal n) {
		if (n.scale() > 0 || n.compareTo(BigDecimal.ZERO) < 0) {
			throw new ArithmeticException("Factorial is only defined for non-negative integers");
		}

		BigDecimal result = BigDecimal.ONE;
		for (int i = 2; i <= n.intValueExact(); i++) {
			result = result.multiply(BigDecimal.valueOf(i));
		}
		return result;
	}

	/**
	 * Computes the natural logarithm (base e) of a value.
	 *
	 * @param x
	 * 	input > 0
	 *
	 * @return ln(x)
	 */
	public BigDecimal ln(BigDecimal x) {
		if (x.compareTo(BigDecimal.ZERO) <= 0)
			throw new ArithmeticException("ln(x) undefined for x <= 0");
		return BigDecimal.valueOf(Math.log(x.doubleValue()));
	}

	/**
	 * Computes the base-10 logarithm of a value.
	 *
	 * @param x
	 * 	input > 0
	 *
	 * @return log₁₀(x)
	 */
	public BigDecimal log10(BigDecimal x) {
		if (x.compareTo(BigDecimal.ZERO) <= 0)
			throw new ArithmeticException("log(x) undefined for x <= 0");
		return BigDecimal.valueOf(Math.log10(x.doubleValue()));
	}

	/**
	 * Computes the sine of a value in the current angle mode (Deg or Rad).
	 *
	 * @param x
	 * 	input angle
	 *
	 * @return sin(x)
	 */
	public BigDecimal sin(BigDecimal x) {
		double radians = toRadians(x);
		return BigDecimal.valueOf(Math.sin(radians));
	}

	/**
	 * Computes the cosine of a value in the current angle mode (Deg or Rad).
	 *
	 * @param x
	 * 	input angle
	 *
	 * @return cos(x)
	 */
	public BigDecimal cos(BigDecimal x) {
		double radians = toRadians(x);
		return BigDecimal.valueOf(Math.cos(radians));
	}

	/**
	 * Computes the tangent of a value in the current angle mode (Deg or Rad).
	 *
	 * @param x
	 * 	input angle
	 *
	 * @return tan(x)
	 */
	public BigDecimal tan(BigDecimal x) {
		double radians = toRadians(x);
		return BigDecimal.valueOf(Math.tan(radians));
	}

	/**
	 * Computes the arcsine of a value and converts the result to current angle mode.
	 *
	 * @param x
	 * 	input value
	 *
	 * @return asin(x)
	 */
	public BigDecimal asin(BigDecimal x) {
		return fromRadians(Math.asin(x.doubleValue()));
	}

	/**
	 * Computes the arccosine of a value and converts the result to current angle mode.
	 *
	 * @param x
	 * 	input value
	 *
	 * @return acos(x)
	 */
	public BigDecimal acos(BigDecimal x) {
		return fromRadians(Math.acos(x.doubleValue()));
	}

	/**
	 * Computes the arctangent of a value and converts the result to current angle mode.
	 *
	 * @param x
	 * 	input value
	 *
	 * @return atan(x)
	 */
	public BigDecimal atan(BigDecimal x) {
		return fromRadians(Math.atan(x.doubleValue()));
	}

	/**
	 * Computes the hyperbolic sine of a value.
	 *
	 * @param x
	 * 	input value
	 *
	 * @return sinh(x)
	 */
	public BigDecimal sinh(BigDecimal x) {
		return BigDecimal.valueOf(Math.sinh(x.doubleValue()));
	}

	/**
	 * Computes the hyperbolic cosine of a value.
	 *
	 * @param x
	 * 	input value
	 *
	 * @return cosh(x)
	 */
	public BigDecimal cosh(BigDecimal x) {
		return BigDecimal.valueOf(Math.cosh(x.doubleValue()));
	}

	/**
	 * Computes the hyperbolic tangent of a value.
	 *
	 * @param x
	 * 	input value
	 *
	 * @return tanh(x)
	 */
	public BigDecimal tanh(BigDecimal x) {
		return BigDecimal.valueOf(Math.tanh(x.doubleValue()));
	}

	/**
	 * Computes the inverse hyperbolic sine (asinh) of a value.
	 *
	 * @param x
	 * 	input value
	 *
	 * @return asinh(x)
	 */
	public BigDecimal asinh(BigDecimal x) {
		return BigDecimal.valueOf(Math.log(x.add(sqrt(x.multiply(x).add(BigDecimal.ONE))).doubleValue()));
	}

	/**
	 * Computes the inverse hyperbolic cosine (acosh) of a value.
	 *
	 * @param x
	 * 	input value (x ≥ 1)
	 *
	 * @return acosh(x)
	 */
	public BigDecimal acosh(BigDecimal x) {
		if (x.compareTo(BigDecimal.ONE) < 0)
			throw new ArithmeticException("acosh undefined for x < 1");
		return BigDecimal.valueOf(Math.log(x.add(sqrt(x.multiply(x).subtract(BigDecimal.ONE))).doubleValue()));
	}

	/**
	 * Computes the inverse hyperbolic tangent (atanh) of a value.
	 *
	 * @param x
	 * 	input value (|x| < 1)
	 *
	 * @return atanh(x)
	 */
	public BigDecimal atanh(BigDecimal x) {
		if (x.abs().compareTo(BigDecimal.ONE) >= 0)
			throw new ArithmeticException("atanh undefined for |x| ≥ 1");
		double d = x.doubleValue();
		return BigDecimal.valueOf(0.5 * Math.log((1 + d) / (1 - d)));
	}

	/**
	 * Converts a BigDecimal angle to radians based on the configured mode (Deg or Rad).
	 *
	 * @param x
	 * 	input angle
	 *
	 * @return angle in radians
	 */
	private double toRadians(BigDecimal x) {
		if (trigonometricMode == TrigonometricMode.DEG) {
			return Math.toRadians(x.doubleValue());
		} else {
			return x.doubleValue();
		}
	}

	/**
	 * Converts a radian value to BigDecimal angle based on the configured mode (Deg or Rad).
	 *
	 * @param radians
	 * 	angle in radians
	 *
	 * @return converted angle as BigDecimal
	 */
	private BigDecimal fromRadians(double radians) {
		if (trigonometricMode == TrigonometricMode.DEG) {
			return BigDecimal.valueOf(Math.toDegrees(radians));
		} else {
			return BigDecimal.valueOf(radians);
		}
	}

}
