package com.mlprograms.justmath.bignumber.internal.math;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.mlprograms.justmath.bignumber.BigNumber;
import lombok.NonNull;

import java.math.MathContext;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.internal.BigNumbers.THREE;
import static com.mlprograms.justmath.bignumber.internal.BigNumbers.TWO;

/**
 * Provides mathematical operations for calculating roots of numbers (radicals).
 */
public class RadicalMath {

	/**
	 * Calculates the square root of the given radicand.
	 * <p>
	 * Mathematically:
	 * <pre>
	 * result = √radicand = radicand^(1/2)
	 * </pre>
	 * The square root is the value which, when multiplied by itself, gives the radicand.
	 *
	 * @param radicand
	 * 	the number to find the square root of, must be non-negative
	 * @param mathContext
	 * 	the {@link MathContext} to control precision and rounding
	 * @param locale
	 * 	the {@link Locale} used for number formatting
	 *
	 * @return the square root of the radicand
	 */
	public static BigNumber squareRoot(@NonNull final BigNumber radicand, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return new BigNumber(BigDecimalMath.root(radicand.toBigDecimal(), TWO.toBigDecimal(), mathContext).toPlainString(), locale);
	}

	/**
	 * Calculates the cubic root (third root) of the given radicand.
	 * <p>
	 * Mathematically:
	 * <pre>
	 * result = ∛radicand = radicand^(1/3)
	 * </pre>
	 * The cubic root is the number which, when raised to the power 3, equals the radicand.
	 *
	 * @param radicand
	 * 	the number to find the cubic root of, can be negative or positive
	 * @param mathContext
	 * 	the {@link MathContext} to control precision and rounding
	 * @param locale
	 * 	the {@link Locale} used for number formatting
	 *
	 * @return the cubic root of the radicand
	 */
	public static BigNumber cubicRoot(@NonNull final BigNumber radicand, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		return new BigNumber(BigDecimalMath.root(radicand.toBigDecimal(), THREE.toBigDecimal(), mathContext).toPlainString(), locale);
	}

	/**
	 * Calculates the nth root of the given radicand.
	 * <p>
	 * Mathematically:
	 * <pre>
	 * result = radicand^(1/index)
	 * </pre>
	 * The nth root is the value which, when raised to the power n (the index), equals the radicand.
	 * <p>
	 * Note: Negative indices or negative radicands are not supported and will throw an exception.
	 *
	 * @param radicand
	 * 	the number to find the root of, must be non-negative
	 * @param index
	 * 	the degree of the root, must be positive
	 * @param mathContext
	 * 	the {@link MathContext} to control precision and rounding
	 * @param locale
	 * 	the {@link Locale} used for number formatting
	 *
	 * @return the nth root of the radicand
	 *
	 * @throws IllegalArgumentException
	 * 	if radicand or index is negative
	 */
	public static BigNumber nthRoot(@NonNull final BigNumber radicand, @NonNull final BigNumber index, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		if (radicand.isNegative() || index.isNegative()) {
			throw new IllegalArgumentException("Cannot calculate nth root with negative index or negative radicand.");
		}
		return new BigNumber(BigDecimalMath.root(radicand.toBigDecimal(), index.toBigDecimal(), mathContext).toPlainString(), locale);
	}

}
