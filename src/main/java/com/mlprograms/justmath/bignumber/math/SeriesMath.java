package com.mlprograms.justmath.bignumber.math;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumbers;
import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;

import java.math.MathContext;
import java.util.Locale;

/**
 * Utility class for performing mathematical series operations with arbitrary precision.
 */
public class SeriesMath {

	/**
	 * Evaluates and prints the result of a summation expression over an integer range, similar to the mathematical
	 * sigma notation ∑ (summation sign). The variable {@code k} is used as the iteration variable in the expression.
	 * <p>
	 * This method takes a start and end value for {@code k}, evaluates the expression {@code kCalculation} for each
	 * integer {@code k} in the range {@code [kStart, kEnd]}, and accumulates the result. The expression must contain
	 * the variable {@code "k"} as a placeholder, which will be replaced by the current value of {@code k} in each
	 * iteration.
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * summenzeichen(new BigNumber("1"), new BigNumber("3"), "2*k + 1", mathContext, TrigonometricMode.RAD, Locale.US);
	 * // Output: 15  → since 2*1+1 + 2*2+1 + 2*3+1 = 3 + 5 + 7 = 15
	 * }</pre>
	 *
	 * @param kStart
	 * 	The lower bound of the summation range (inclusive). Must be an integer.
	 * @param kEnd
	 * 	The upper bound of the summation range (inclusive). Must be an integer and not less than {@code kStart}.
	 * @param kCalculation
	 * 	A mathematical expression as a string that includes the variable {@code "k"}.
	 * 	This expression is evaluated for each value of {@code k} from {@code kStart} to {@code kEnd}.
	 * @param mathContext
	 * 	The {@link MathContext} to define precision and rounding for the calculations.
	 * @param trigonometricMode
	 * 	The {@link TrigonometricMode} (e.g., RAD or DEG) used by the calculator engine if trigonometric functions are
	 * 	involved.
	 * @param locale
	 * 	The {@link Locale} used to format {@code BigNumber} values (e.g., for decimal separators).
	 *
	 * @throws IllegalArgumentException
	 * 	If {@code kCalculation} does not contain the variable {@code "k"}.
	 * @throws IllegalArgumentException
	 * 	If {@code kStart} is greater than {@code kEnd}.
	 * @throws IllegalArgumentException
	 * 	If either {@code kStart} or {@code kEnd} is not an integer.
	 * @see CalculatorEngine
	 * @see BigNumber
	 * @see TrigonometricMode
	 */
	// TODO:
	//  - in ExpressionElements hinzufügen -> ThreeArgumentFunction (oder so) hinzufügen
	//  - Tokenizer anpassen, so das er ∑(number1; number2; berechnung) tokenisieren kann
	//    -> vielleicht auch den parser und den evaluator anpassen?
	//  - Tests schreiben
	public static BigNumber summation(BigNumber kStart, BigNumber kEnd, String kCalculation, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
		CalculatorEngine calculatorEngine = new CalculatorEngine(mathContext, trigonometricMode);

		if (!kCalculation.contains("k")) {
			throw new IllegalArgumentException("kCalculation must contain the variable 'k'");
		}

		if (kStart.isGreaterThan(kEnd)) {
			throw new IllegalArgumentException("kEnd must be greater than or equal to kStart");
		}

		if (!kStart.isInteger() || !kEnd.isInteger()) {
			throw new IllegalArgumentException("kStart and kEnd must be an integer");
		}

		BigNumber result = BigNumbers.ZERO;

		while (kStart.isLessThanOrEqualTo(kEnd)) {
			BigNumber currentCalculation = calculatorEngine.evaluate(kCalculation.replace("k", kStart.toString(locale)));
			result = result.add(currentCalculation);
			kStart = kStart.add(BigNumbers.ONE);
		}

		return new BigNumber(result, locale, mathContext, trigonometricMode);
	}

}
