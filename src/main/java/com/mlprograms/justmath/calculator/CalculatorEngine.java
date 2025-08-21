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

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import com.mlprograms.justmath.calculator.internal.expression.ExpressionElements;
import com.mlprograms.justmath.calculator.internal.token.Token;
import lombok.Getter;
import lombok.NonNull;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mlprograms.justmath.bignumber.BigNumbers.DEFAULT_DIVISION_PRECISION;

/**
 * ExactCalculatorEngine.java
 * <p>
 * Main entry point for evaluating mathematical expressions as strings with exact precision.
 * Converts the input to tokens, parses them to postfix (RPN), and evaluates the result.
 */
@Getter
public class CalculatorEngine {

    /**
     * Static thread-local storage for the current variables in the evaluation context.
     * This allows nested evaluations to access variables from the outer context.
     */
    private static final ThreadLocal<Map<String, BigNumber>> currentVariables = ThreadLocal.withInitial(HashMap::new);

    /**
     * Tokenizer instance used to convert input expressions into tokens.
     */
    private final Tokenizer tokenizer;
    /**
     * Evaluator instance used to compute the result from postfix token lists.
     */
    private final Evaluator evaluator;
    /**
     * Parser instance used to convert infix token lists to postfix notation.
     */
    private final Parser parser;

    public CalculatorEngine() {
        this(getDefaultMathContext(DEFAULT_DIVISION_PRECISION), TrigonometricMode.DEG);
    }

    /**
     * Constructs a CalculatorEngine with the specified division precision and default trigonometric mode (DEG).
     *
     * @param divisionPrecision the precision for division operations
     */
    public CalculatorEngine(int divisionPrecision) {
        this(getDefaultMathContext(divisionPrecision), TrigonometricMode.DEG);
    }

    /**
     * Constructs a CalculatorEngine with the specified division precision and trigonometric mode.
     *
     * @param divisionPrecision the precision for division operations
     * @param trigonometricMode the trigonometric mode (DEG or RAD)
     */
    public CalculatorEngine(int divisionPrecision, @NonNull TrigonometricMode trigonometricMode) {
        this(getDefaultMathContext(divisionPrecision), trigonometricMode);
    }

    /**
     * Constructs a CalculatorEngine with the specified MathContext and default trigonometric mode (DEG).
     *
     * @param mathContext the MathContext specifying precision and rounding mode
     */
    public CalculatorEngine(@NonNull MathContext mathContext) {
        this(mathContext, TrigonometricMode.DEG);
    }

    /**
     * Constructs a CalculatorEngine with the specified trigonometric mode and default MathContext (precision 1000).
     *
     * @param trigonometricMode the trigonometric mode (DEG or RAD)
     */
    public CalculatorEngine(@NonNull TrigonometricMode trigonometricMode) {
        this(getDefaultMathContext(DEFAULT_DIVISION_PRECISION), trigonometricMode);
    }

    /**
     * Constructs a CalculatorEngine with the specified MathContext and trigonometric mode.
     * Initializes the tokenizer, evaluator, and parser components.
     *
     * @param mathContext       the MathContext specifying precision and rounding mode
     * @param trigonometricMode the trigonometric mode (DEG or RAD)
     */
    public CalculatorEngine(@NonNull MathContext mathContext, @NonNull TrigonometricMode trigonometricMode) {
        this.tokenizer = new Tokenizer();
        this.evaluator = new Evaluator(mathContext, trigonometricMode);
        this.parser = new Parser();
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

    /**
     * Gets the current variables in the evaluation context.
     * This includes variables from outer evaluation contexts in nested evaluations.
     *
     * @return a map of variable names with their BigNumber values
     */
    public static Map<String, BigNumber> getCurrentVariables() {
        return new HashMap<>(currentVariables.get());
    }

    /**
     * Evaluates a given mathematical expression with full BigDecimal precision.
     *
     * @param expression input string expression (e.g. "3.5 + sqrt(2)")
     * @return result as string, rounded if necessary
     */
    public BigNumber evaluate(@NonNull String expression) {
        return evaluate(expression, Map.of());
    }

    /**
     * Evaluates a mathematical expression with optional variable substitution.
     *
     * @param expression the input string expression to evaluate
     * @param variables  a map of variable names with their BigNumber values
     * @return the result as a BigNumber, trimmed of trailing zeros
     */
    public BigNumber evaluate(@NonNull final String expression, @NonNull final Map<String, BigNumber> variables) {
        // Store the current variables in the thread-local storage
        Map<String, BigNumber> combinedVariables = new HashMap<>(getCurrentVariables());
        combinedVariables.putAll(variables);
        currentVariables.set(combinedVariables);

        // Replace the |n| in the expression to look like abs(n)
        String expressionWithoutAbsValueSign = replaceAbsSigns(expression);

        // Tokenize the input string
        List<Token> tokens = tokenizer.tokenize(expressionWithoutAbsValueSign);

        replaceVariables(tokens, combinedVariables);

        // Parse to postfix notation using shunting yard algorithm
        List<Token> postfix = parser.toPostfix(tokens);

        // Evaluate the postfix expression to a BigDecimal result
        return evaluator.evaluate(postfix).trim();
    }

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
    private String replaceAbsSigns(String expression) {
        String absValueSign = ExpressionElements.SURRFUNC_ABS_S;

        int occurrences = countOccurrences(expression, absValueSign);
        if (occurrences % 2 != 0) {
            throw new IllegalArgumentException("The expression must contain an even number (>1) of abs sign functions");
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
    private int countOccurrences(@NonNull final String text, @NonNull final String search) {
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
    private void replaceVariables(List<Token> tokens, Map<String, BigNumber> variables) {
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
