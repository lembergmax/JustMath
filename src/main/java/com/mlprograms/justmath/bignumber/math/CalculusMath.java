/*
 * Copyright (c) 2025 Max Lemberg
 *
 * This file is part of JustMath.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
			Map<String, String> vars = new HashMap<>(getCurrentVariables());
			vars.put("x", x.toString());

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
