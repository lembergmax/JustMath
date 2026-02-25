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

package com.mlprograms.justmath.calculator;

import com.mlprograms.justmath.bignumber.BigNumbers;
import com.mlprograms.justmath.calculator.exceptions.CyclicVariableReferenceException;
import com.mlprograms.justmath.calculator.expression.ExpressionElements;
import com.mlprograms.justmath.calculator.internal.Token;

import lombok.NonNull;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

public class CalculatorEngineUtils {

    /**
     * Replaces all occurrences of absolute value signs in a mathematical expression
     * with explicit function-style absolute value notation.
     * <p>
     * In many mathematical notations, absolute values are written using vertical bars, e.g. {@code |x+3|}.
     * Internally, however, this implementation represents absolute value as a function call
     * (e.g. {@code abs(x+3)}). This method ensures that each absolute value sign is consistently
     * converted to its functional form.
     * </p>
     *
     * <p>
     * The replacement follows this rule:
     * <ul>
     *   <li>The first occurrence of the absolute sign ({@link ExpressionElements#SURRFUNC_ABS_S})
     *       is replaced with {@code abs(} (opening the absolute value function).</li>
     *   <li>The second occurrence is replaced with a closing parenthesis {@code )}.</li>
     *   <li>The third again with {@code abs(}, the fourth with {@code )}, and so forth.</li>
     * </ul>
     * As a result, an even number of absolute signs is required to form valid pairs.
     * </p>
     *
     * <p>
     * For example:
     * <ul>
     *   <li>Input: {@code |x+2|} → Output: {@code abs(x+2)}</li>
     *   <li>Input: {@code |x-1| + |y|} → Output: {@code abs(x-1) + abs(y)}</li>
     * </ul>
     * </p>
     *
     * @param expression the mathematical expression containing absolute value signs
     * @return the expression with all absolute signs replaced by {@code abs(...)} notation
     * @throws IllegalArgumentException if the expression contains an odd number of
     *                                  absolute value signs, since this would result in unbalanced expressions
     *                                  (e.g., {@code |x+3}).
     */
    public static String replaceAbsSigns(String expression) {
        String absValueSign = ExpressionElements.SURRFUNC_ABS_S;

        int occurrences = countOccurrences(expression, absValueSign);
        if (occurrences % 2 != 0) {
            throw new IllegalArgumentException("Expression must contain an even number (greater than 1) of abs sign functions ('|')");
        }

        StringBuilder result = new StringBuilder();
        int index = 0;
        int foundCount = 0;

        while (index < expression.length()) {
            int next = expression.indexOf(absValueSign, index);
            if (next == -1) {
                result.append(expression.substring(index));
                break;
            }

            result.append(expression, index, next);

            if (foundCount % 2 == 0) {
                result.append(ExpressionElements.FUNC_ABS + ExpressionElements.PAR_LEFT);
            } else {
                result.append(")");
            }

            foundCount++;
            index = next + absValueSign.length();
        }

        return result.toString();
    }

    /**
     * Counts the number of occurrences of a given substring within a text.
     * <p>
     * This method performs a non-overlapping search for the {@code search} string
     * within the {@code text}, moving forward after each found occurrence.
     * </p>
     *
     * @param text   the input string to be scanned
     * @param search the substring to search for
     * @return the number of non-overlapping occurrences of {@code search} within {@code text};
     * returns {@code -1} if either {@code text} or {@code search} is empty
     */
    static int countOccurrences(@NonNull final String text, @NonNull final String search) {
        if (text.isEmpty() || search.isEmpty()) {
            return -1;
        }

        int count = 0;
        int index = 0;
        while ((index = text.indexOf(search, index)) != -1) {
            count++;
            index += search.length();
        }

        return count;
    }

    /**
     * Replaces variable tokens in the provided list with their corresponding values from the variable map.
     * If a variable is not defined in the map, throws an IllegalArgumentException.
     *
     * @param tokens    the list of tokens to process and replace variables in
     * @param variables a map of variable names with their BigNumber values
     * @throws IllegalArgumentException if a variable token does not have a corresponding value in the map
     */
    static void replaceVariables(@NonNull final CalculatorEngine calculatorEngine, @NonNull final List<Token> tokens, @NonNull final Map<String, String> variables) {
        checkVariablesForRecursion(calculatorEngine, variables);

        for (int i = 0; i < tokens.size(); i++) {
            final Token token = tokens.get(i);
            if (token.getType() == Token.Type.VARIABLE) {
                final String value = variables.get(token.getValue());

                if (value == null || value.isBlank()) {
                    throw new IllegalArgumentException("Variable '" + token.getValue() + "' is not defined.");
                }

                // Add zero to the evaluated variable value to coerce coordinate-style results into a single numeric value.
                // Example: evaluated value = "r=5; θ=53.13010235" -> "(r=5; θ=53.13010235) + 0 = 5"
                final String evaluatedVariableValue = calculatorEngine.evaluate(value, variables).add(BigNumbers.ZERO).toString();
                tokens.set(i, new Token(Token.Type.NUMBER, evaluatedVariableValue));
            }
        }
    }

    static void checkVariablesForRecursion(@NonNull final CalculatorEngine calculatorEngine, @NonNull final Map<String, String> variables) {
        Set<String> visitedVariables = new HashSet<>();
        Set<String> currentPath = new HashSet<>();

        for (final String variableName : variables.keySet()) {
            checkVariable(calculatorEngine, variableName, variables, visitedVariables, currentPath);
        }
    }

    private static void checkVariable(@NonNull final CalculatorEngine calculatorEngine, @NonNull final String variableName, @NonNull final Map<String, String> variables, @NonNull final Set<String> visitedVariables, @NonNull final Set<String> currentPath) {
        if (currentPath.contains(variableName)) {
            throw new CyclicVariableReferenceException("Cyclic variable reference detected in: " + variableName);
        }

        if (visitedVariables.contains(variableName)) {
            return;
        }

        currentPath.add(variableName);

        final String expression = variables.get(variableName);
        if (expression != null && !expression.isEmpty()) {
            final List<Token> tokens = calculatorEngine.getTokenizer().tokenize(expression);

            for (final Token token : tokens) {
                if (token.getType() != Token.Type.VARIABLE) {
                    continue;
                }

                final String referencedVar = token.getValue();
                checkVariable(calculatorEngine, referencedVar, variables, visitedVariables, currentPath);
            }
        }

        currentPath.remove(variableName);
        visitedVariables.add(variableName);
    }

    /**
     * Returns a default MathContext with the specified division precision and RoundingMode.HALF_UP.
     *
     * @param divisionPrecision the precision for division operations
     * @return a MathContext instance with the given precision and HALF_UP rounding mode
     */
    public static MathContext getDefaultMathContext(int divisionPrecision) {
        return new MathContext(divisionPrecision, RoundingMode.HALF_UP);
    }

}
