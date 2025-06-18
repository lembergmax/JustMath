package com.mlprograms.justmath.bignumber.internal.math;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.mlprograms.justmath.bignumber.BigNumber;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.internal.BigNumberValues.*;

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
		return nthRoot(radicand, TWO, mathContext, locale);
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
		return nthRoot(radicand, THREE, mathContext, locale);
	}

	public static BigNumber nthRoot(@NonNull final BigNumber radicand, @NonNull final BigNumber index, @NonNull final MathContext mathContext, @NonNull final Locale locale) {
		if (index.isEqualTo(ZERO)) {
			throw new IllegalArgumentException("Index must not be zero");
		}

		// Fall: negativer Index → berechne positive Wurzel und bilde den Kehrwert
		if (index.isNegative()) {
			BigNumber positiveIndex = index.negate();
			BigNumber positiveRoot = nthRoot(radicand, positiveIndex, mathContext, locale);
			return ONE.divide(positiveRoot, mathContext).trim();
		}

		boolean isEvenRoot = index.isInteger() && index.toBigDecimal().remainder(BigDecimal.valueOf(2)).compareTo(BigDecimal.ZERO) == 0;
		boolean radicandIsNegative = radicand.isNegative();

		// Gerade Wurzel von negativer Zahl → nicht erlaubt
		if (radicandIsNegative && isEvenRoot) {
			throw new IllegalArgumentException("Even root of a negative number is not a real number");
		}

		// Ungerade Wurzel von negativer Zahl → Ergebnis ist negativ
		if (radicandIsNegative) {
			BigDecimal absValue = radicand.toBigDecimal().negate();  // |-x|
			BigDecimal root = BigDecimalMath.root(absValue, index.toBigDecimal(), mathContext);
			return new BigNumber(root.negate().toPlainString(), locale, mathContext).trim();
		}

		// Normale positive Wurzel
		BigDecimal result = BigDecimalMath.root(radicand.toBigDecimal(), index.toBigDecimal(), mathContext);
		return new BigNumber(result.toPlainString(), locale, mathContext).trim();
	}

}
