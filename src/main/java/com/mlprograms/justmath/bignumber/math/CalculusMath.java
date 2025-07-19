package com.mlprograms.justmath.bignumber.math;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumbers;
import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import lombok.NonNull;

import java.math.MathContext;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.mlprograms.justmath.calculator.CalculatorEngine.getCurrentVariables;

public class CalculusMath {

	// TODO: integral
	public static BigNumber integrate(@NonNull BigNumber from, @NonNull BigNumber to, @NonNull String kExpression, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
		if (from.isGreaterThan(to)) {
			throw new IllegalArgumentException("Lower bound must be less than or equal to upper bound");
		}
		if (!kExpression.contains("x")) {
			throw new IllegalArgumentException("Expression must contain the variable 'x'");
		}

		final int steps = 10000;
		final CalculatorEngine calculator = new CalculatorEngine(mathContext, trigonometricMode);

		BigNumber h = to.subtract(from).divide(new BigNumber(String.valueOf(steps)), mathContext);
		BigNumber result = BigNumbers.ZERO;

		for (int i = 0; i <= steps; i++) {
			BigNumber x = from.add(h.multiply(new BigNumber(String.valueOf(i))));
			Map<String, BigNumber> vars = new HashMap<>(getCurrentVariables());
			vars.put("x", x);

			BigNumber fx = calculator.evaluate(kExpression, vars);
			if (i == 0 || i == steps) {
				result = result.add(fx.divide(BigNumbers.TWO, mathContext));
			} else {
				result = result.add(fx);
			}
		}

		result = result.multiply(h).round(mathContext);
		return new BigNumber(result, locale, mathContext, trigonometricMode);
	}


}
