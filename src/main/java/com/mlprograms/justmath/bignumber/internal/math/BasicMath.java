package com.mlprograms.justmath.bignumber.internal.math;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.internal.BigNumbers;

import java.math.MathContext;
import java.util.Locale;

import static com.mlprograms.justmath.bignumber.internal.BigNumbers.ZERO;

public class BasicMath {

	public static BigNumber add(BigNumber augend, BigNumber addend) {
		return new BigNumber(augend.toBigDecimal().add(addend.toBigDecimal()).toPlainString());
	}

	public static BigNumber subtract(BigNumber minuend, BigNumber subtrahend) {
		return new BigNumber(minuend.toBigDecimal().subtract(subtrahend.toBigDecimal()).toPlainString());
	}

	public static BigNumber multiply(BigNumber multiplicand, BigNumber multiplier) {
		return new BigNumber(multiplicand.toBigDecimal().multiply(multiplier.toBigDecimal()).toPlainString());
	}

	public static BigNumber divide(BigNumber dividend, BigNumber divisor, MathContext mathContext) {
		if (divisor.compareTo(BigNumbers.ZERO) == 0) {
			throw new ArithmeticException("Division by zero");
		}

		return new BigNumber(dividend.toBigDecimal().divide(divisor.toBigDecimal(), mathContext).toPlainString());

	}

	public static BigNumber modulo(BigNumber dividend, BigNumber divisor) {
		if (divisor.isEqualTo(ZERO)) {
			throw new IllegalArgumentException("Cannot perform modulo operation with divisor zero.");
		}

		if (dividend.isNegative() || divisor.isNegative()) {
			throw new IllegalArgumentException("Modulo operation requires both numbers to be non-negative.");
		}

		BigNumber remainder = dividend.clone();

		while (remainder.isGreaterThanOrEqualTo(divisor)) {
			remainder = remainder.subtract(divisor);
		}

		return remainder;
	}

	public static BigNumber power(BigNumber base, BigNumber exponent, MathContext mathContext, Locale locale) {
		return new BigNumber(BigDecimalMath.pow(base.toBigDecimal(), exponent.toBigDecimal(), mathContext).toPlainString(), locale);
	}

	public static BigNumber factorial(BigNumber argument, MathContext mathContext, Locale locale) {
		return new BigNumber(BigDecimalMath.factorial(argument.toBigDecimal(), mathContext).toPlainString(), locale);
	}

}