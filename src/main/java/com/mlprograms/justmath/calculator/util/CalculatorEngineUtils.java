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

package com.mlprograms.justmath.calculator.util;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.internal.expression.ExpressionElements;
import com.mlprograms.justmath.calculator.internal.token.Token;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

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
    public static int countOccurrences(@NonNull final String text, @NonNull final String search) {
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
    public static void replaceVariables(List<Token> tokens, Map<String, BigNumber> variables) {
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.getType() == Token.Type.VARIABLE) {
                BigNumber value = variables.get(token.getValue());
                if (value == null) {
                    throw new IllegalArgumentException("Variable '" + token.getValue() + "' is not defined.");
                }
                tokens.set(i, new Token(Token.Type.NUMBER, value.toString()));
            }
        }
    }

}
