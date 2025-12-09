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
import com.mlprograms.justmath.bignumber.math.utils.MathUtils;
import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import com.mlprograms.justmath.calculator.internal.expression.ExpressionElements;

import lombok.NonNull;

import java.math.MathContext;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.mlprograms.justmath.calculator.CalculatorEngine.getCurrentVariables;

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
     * summation(new BigNumber("1"), new BigNumber("3"), "2*k + 1", mathContext, TrigonometricMode.RAD, Locale.US);
     * // Output: 15  → since 2*1+1 + 2*2+1 + 2*3+1 = 3 + 5 + 7 = 15
     * }</pre>
     *
     * @param kStart            The lower bound of the summation range (inclusive). Must be an integer.
     * @param kEnd              The upper bound of the summation range (inclusive). Must be an integer and not less than {@code kStart}.
     * @param kCalculation      A mathematical expression as a string that includes the variable {@code "k"}.
     *                          This expression is evaluated for each value of {@code k} from {@code kStart} to {@code kEnd}.
     * @param mathContext       The {@link MathContext} to define precision and rounding for the calculations.
     * @param trigonometricMode The {@link TrigonometricMode} (e.g., RAD or DEG) used by the calculator engine if trigonometric functions are
     *                          involved.
     * @param locale            The {@link Locale} used to format {@code BigNumber} values (e.g., for decimal separators).
     * @throws IllegalArgumentException If {@code kCalculation} does not contain the variable {@code "k"}.
     * @throws IllegalArgumentException If {@code kStart} is greater than {@code kEnd}.
     * @throws IllegalArgumentException If either {@code kStart} or {@code kEnd} is not an integer.
     * @see CalculatorEngine
     * @see BigNumber
     * @see TrigonometricMode
     */
    public static BigNumber summation(@NonNull final BigNumber kStart, @NonNull final BigNumber kEnd, @NonNull final String kCalculation, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
        return summation(kStart, kEnd, kCalculation, mathContext, trigonometricMode, locale, Map.of());
    }

    /**
     * Evaluates and prints the result of a summation expression over an integer range, similar to the mathematical
     * sigma notation ∑ (summation sign). The variable {@code k} is used as the iteration variable in the expression.
     * <p>
     * This method takes a start and end value for {@code k}, evaluates the expression {@code kCalculation} for each
     * integer {@code k} in the range {@code [kStart, kEnd]}, and accumulates the result. The expression must contain
     * the variable {@code "k"} as a placeholder, which will be replaced by the current value of {@code k} in each
     * iteration.
     * <p>
     * This overload allows passing external variables that can be used in the calculation alongside the 'k' variable.
     * <p>
     * Example usage:
     * <pre>{@code
     * Map<String, BigNumber> variables = Map.of("a", new BigNumber("0.5"));
     * summation(new BigNumber("0"), new BigNumber("4"), "2^(k!-k*a)", mathContext, TrigonometricMode.RAD, Locale.US, variables);
     * }</pre>
     *
     * @param kStart            The lower bound of the summation range (inclusive). Must be an integer.
     * @param kEnd              The upper bound of the summation range (inclusive). Must be an integer and not less than {@code kStart}.
     * @param kCalculation      A mathematical expression as a string that includes the variable {@code "k"}.
     *                          This expression is evaluated for each value of {@code k} from {@code kStart} to {@code kEnd}.
     * @param mathContext       The {@link MathContext} to define precision and rounding for the calculations.
     * @param trigonometricMode The {@link TrigonometricMode} (e.g., RAD or DEG) used by the calculator engine if trigonometric functions are
     *                          involved.
     * @param locale            The {@link Locale} used to format {@code BigNumber} values (e.g., for decimal separators).
     * @param externalVariables A map of external variable names with their BigNumber values that can be used in the calculation.
     * @throws IllegalArgumentException If {@code kCalculation} does not contain the variable {@code "k"}.
     * @throws IllegalArgumentException If {@code kStart} is greater than {@code kEnd}.
     * @throws IllegalArgumentException If either {@code kStart} or {@code kEnd} is not an integer.
     * @see CalculatorEngine
     * @see BigNumber
     * @see TrigonometricMode
     */
    public static BigNumber summation(@NonNull final BigNumber kStart, @NonNull final BigNumber kEnd, @NonNull final String kCalculation, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale, @NonNull final Map<String, String> externalVariables) {
        checkParams(kStart, kEnd, kCalculation, mathContext, externalVariables);

        CalculatorEngine calculatorEngine = new CalculatorEngine(mathContext, trigonometricMode);

        BigNumber result = BigNumbers.ZERO;
        BigNumber kStartClone = kStart.clone();

        while (kStartClone.isLessThanOrEqualTo(kEnd)) {
            Map<String, String> combinedVariables = new HashMap<>(getCurrentVariables());
            combinedVariables.putAll(externalVariables);
            combinedVariables.put(ExpressionElements.K_SERIES_MATH_VARIABLE, kStartClone.toString());

            BigNumber currentCalculation = calculatorEngine.evaluate(kCalculation, combinedVariables);
            result = result.add(currentCalculation);
            kStartClone = kStartClone.add(BigNumbers.ONE);
        }

        return new BigNumber(result, locale, mathContext, trigonometricMode);
    }

    /**
     * Computes the product (multiplicative accumulation) of values obtained by evaluating a given
     * mathematical expression over an integer range of the iteration variable {@code k}.
     * <p>
     * This method is analogous to the mathematical product notation (∏), defined as:
     * <pre>
     *   ∏_{k = kStart}^{kEnd} f(k)
     * </pre>
     * where <code>f(k)</code> is the function or expression given by {@code kCalculation}.
     * The result is the sequential multiplication of each evaluated term for integer values of
     * {@code k} from {@code kStart} up to and including {@code kEnd}.
     * <p>
     * The iteration variable {@code k} is substituted into the expression {@code kCalculation} at each step,
     * and the evaluated result is multiplied into the accumulating product.
     * <p>
     * <b>Parameters:</b>
     * <ul>
     *   <li>{@code kStart} - The start value of the iteration variable {@code k} (inclusive).</li>
     *   <li>{@code kEnd} - The end value of the iteration variable {@code k} (inclusive).</li>
     *   <li>{@code kCalculation} - A string representing a mathematical expression to be evaluated at each
     *       value of {@code k}. This expression may involve standard arithmetic operations, trigonometric
     *       functions, and external variables.</li>
     *   <li>{@code mathContext} - The {@link MathContext} defining precision and rounding mode for
     *       numerical calculations, ensuring high-precision arithmetic.</li>
     *   <li>{@code trigonometricMode} - The mode in which trigonometric functions are evaluated (e.g., radians or degrees).</li>
     *   <li>{@code locale} - The locale used to correctly parse and format numbers, considering
     *       decimal separators and digit grouping.</li>
     *   <li>{@code externalVariables} - A map of variable names to {@link BigNumber} values which can
     *       be referenced inside {@code kCalculation} alongside the iteration variable {@code k}.</li>
     * </ul>
     * <p>
     * <b>Mathematical Context:</b><br>
     * The product notation ∏ is fundamental in many areas of mathematics, such as factorials, binomial coefficients,
     * infinite products in analysis, and product formulas in number theory.
     * <p>
     * This implementation performs a discrete finite product over an integer range. For example, computing
     * the factorial of a number {@code n} can be expressed as
     * <pre>
     *   factorial(n) = ∏_{k=1}^{n} k
     * </pre>
     * by setting {@code kCalculation} to the variable {@code "k"}.
     * <p>
     * The method supports complex expressions for {@code kCalculation}, allowing calculations such as
     * <pre>
     *   ∏_{k=1}^{5} (2*k + 1)
     * </pre>
     * or trigonometric expressions like
     * <pre>
     *   ∏_{k=0}^{3} sin(k * π / 4)
     * </pre>
     * where the trigonometric mode will influence the evaluation of sine.
     * <p>
     * <b>Implementation Details:</b><br>
     * Internally, the method:
     * <ol>
     *   <li>Initializes the product result to 1 (multiplicative identity).</li>
     *   <li>Iterates from {@code kStart} to {@code kEnd} (inclusive) in integer steps.</li>
     *   <li>At each iteration, merges any external variables with the current iteration variable {@code k}.</li>
     *   <li>Evaluates {@code kCalculation} using a {@link CalculatorEngine} configured with the given
     *       {@code mathContext} and {@code trigonometricMode}.</li>
     *   <li>Multiplies the evaluated value into the cumulative product.</li>
     * </ol>
     * <p>
     * If {@code kStart} is greater than {@code kEnd}, the product returns the multiplicative identity {@code 1}.
     * <p>
     *
     * @param kStart            The start integer value of {@code k} (inclusive).
     * @param kEnd              The end integer value of {@code k} (inclusive).
     * @param kCalculation      The string expression to evaluate for each {@code k}.
     * @param mathContext       The MathContext defining precision and rounding.
     * @param trigonometricMode The trigonometric mode (e.g., radians or degrees).
     * @param locale            The locale for parsing and formatting numbers.
     * @return The computed product as a {@link BigNumber}, representing
     * \(\prod_{k=kStart}^{kEnd} f(k)\).
     * @throws IllegalArgumentException if parameters are invalid or the expression cannot be evaluated.
     */
    public static BigNumber product(@NonNull final BigNumber kStart, @NonNull final BigNumber kEnd, @NonNull final String kCalculation, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale) {
        return product(kStart, kEnd, kCalculation, mathContext, trigonometricMode, locale, Map.of());
    }

    /**
     * Computes the product (multiplicative accumulation) of values obtained by evaluating a given
     * mathematical expression over an integer range of the iteration variable {@code k}.
     * <p>
     * This method is analogous to the mathematical product notation (∏), defined as:
     * <pre>
     *   ∏_{k = kStart}^{kEnd} f(k)
     * </pre>
     * where <code>f(k)</code> is the function or expression given by {@code kCalculation}.
     * The result is the sequential multiplication of each evaluated term for integer values of
     * {@code k} from {@code kStart} up to and including {@code kEnd}.
     * <p>
     * The iteration variable {@code k} is substituted into the expression {@code kCalculation} at each step,
     * and the evaluated result is multiplied into the accumulating product.
     * <p>
     * <b>Parameters:</b>
     * <ul>
     *   <li>{@code kStart} - The start value of the iteration variable {@code k} (inclusive).</li>
     *   <li>{@code kEnd} - The end value of the iteration variable {@code k} (inclusive).</li>
     *   <li>{@code kCalculation} - A string representing a mathematical expression to be evaluated at each
     *       value of {@code k}. This expression may involve standard arithmetic operations, trigonometric
     *       functions, and external variables.</li>
     *   <li>{@code mathContext} - The {@link MathContext} defining precision and rounding mode for
     *       numerical calculations, ensuring high-precision arithmetic.</li>
     *   <li>{@code trigonometricMode} - The mode in which trigonometric functions are evaluated (e.g., radians or degrees).</li>
     *   <li>{@code locale} - The locale used to correctly parse and format numbers, considering
     *       decimal separators and digit grouping.</li>
     *   <li>{@code externalVariables} - A map of variable names to {@link BigNumber} values which can
     *       be referenced inside {@code kCalculation} alongside the iteration variable {@code k}.</li>
     * </ul>
     * <p>
     * <b>Mathematical Context:</b><br>
     * The product notation ∏ is fundamental in many areas of mathematics, such as factorials, binomial coefficients,
     * infinite products in analysis, and product formulas in number theory.
     * <p>
     * This implementation performs a discrete finite product over an integer range. For example, computing
     * the factorial of a number {@code n} can be expressed as
     * <pre>
     *   factorial(n) = ∏_{k=1}^{n} k
     * </pre>
     * by setting {@code kCalculation} to the variable {@code "k"}.
     * <p>
     * The method supports complex expressions for {@code kCalculation}, allowing calculations such as
     * <pre>
     *   ∏_{k=1}^{5} (2*k + 1)
     * </pre>
     * or trigonometric expressions like
     * <pre>
     *   ∏_{k=0}^{3} sin(k * π / 4)
     * </pre>
     * where the trigonometric mode will influence the evaluation of sine.
     * <p>
     * <b>Implementation Details:</b><br>
     * Internally, the method:
     * <ol>
     *   <li>Initializes the product result to 1 (multiplicative identity).</li>
     *   <li>Iterates from {@code kStart} to {@code kEnd} (inclusive) in integer steps.</li>
     *   <li>At each iteration, merges any external variables with the current iteration variable {@code k}.</li>
     *   <li>Evaluates {@code kCalculation} using a {@link CalculatorEngine} configured with the given
     *       {@code mathContext} and {@code trigonometricMode}.</li>
     *   <li>Multiplies the evaluated value into the cumulative product.</li>
     * </ol>
     * <p>
     * If {@code kStart} is greater than {@code kEnd}, the product returns the multiplicative identity {@code 1}.
     * <p>
     *
     * @param kStart            The start integer value of {@code k} (inclusive).
     * @param kEnd              The end integer value of {@code k} (inclusive).
     * @param kCalculation      The string expression to evaluate for each {@code k}.
     * @param mathContext       The MathContext defining precision and rounding.
     * @param trigonometricMode The trigonometric mode (e.g., radians or degrees).
     * @param locale            The locale for parsing and formatting numbers.
     * @param externalVariables Additional variables available in the expression besides {@code k}.
     * @return The computed product as a {@link BigNumber}, representing
     * \(\prod_{k=kStart}^{kEnd} f(k)\).
     * @throws IllegalArgumentException if parameters are invalid or the expression cannot be evaluated.
     */
    public static BigNumber product(@NonNull final BigNumber kStart, @NonNull final BigNumber kEnd, @NonNull final String kCalculation, @NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode, @NonNull final Locale locale, @NonNull final Map<String, String> externalVariables) {
        checkParams(kStart, kEnd, kCalculation, mathContext, externalVariables);

        CalculatorEngine calculatorEngine = new CalculatorEngine(mathContext, trigonometricMode);

        BigNumber result = BigNumbers.ONE;
        BigNumber kStartClone = kStart.clone();

        while (kStartClone.isLessThanOrEqualTo(kEnd)) {
            Map<String, String> combinedVariables = new HashMap<>(getCurrentVariables());
            combinedVariables.putAll(externalVariables);
            combinedVariables.put(ExpressionElements.K_SERIES_MATH_VARIABLE, kStartClone.toString());

            BigNumber currentCalculation = calculatorEngine.evaluate(kCalculation, combinedVariables);
            result = result.multiply(currentCalculation);
            kStartClone = kStartClone.add(BigNumbers.ONE);
        }

        return new BigNumber(result, locale, mathContext, trigonometricMode);
    }

    /**
     * Validates the parameters for series operations.
     *
     * @param kStart       The lower bound of the range (inclusive).
     * @param kEnd         The upper bound of the range (inclusive).
     * @param kCalculation The mathematical expression as a string must contain the variable "k".
     * @param mathContext  The MathContext specifying precision and rounding.
     * @throws IllegalArgumentException if:
     *                                  - kCalculation does not contain "k"
     *                                  - kStart is greater than kEnd
     *                                  - kStart, or kEnd is not an integer
     */
    private static void checkParams(BigNumber kStart, BigNumber kEnd, String kCalculation, MathContext mathContext, Map<String, String> externalVariables) {
        MathUtils.checkMathContext(mathContext);

        if (!kCalculation.contains(ExpressionElements.K_SERIES_MATH_VARIABLE)) {
            throw new IllegalArgumentException("Expression must include the variable '" + ExpressionElements.K_SERIES_MATH_VARIABLE + "'.");
        }

        if (kStart.isGreaterThan(kEnd)) {
            throw new IllegalArgumentException("End value must be greater than or equal to the start value.");
        }

        if (!kStart.isInteger() || !kEnd.isInteger()) {
            throw new IllegalArgumentException("Start and end values must be integers.");
        }

        if (externalVariables.containsKey(ExpressionElements.K_SERIES_MATH_VARIABLE)) {
            throw new IllegalArgumentException("External variables must not use the reserved name '" + ExpressionElements.K_SERIES_MATH_VARIABLE + "'.");
        }
    }

}
