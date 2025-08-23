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
import com.mlprograms.justmath.bignumber.BigNumbers;
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
import static com.mlprograms.justmath.calculator.util.CalculatorEngineUtils.replaceAbsSigns;
import static com.mlprograms.justmath.calculator.util.CalculatorEngineUtils.replaceVariables;

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
    private final PostfixParser postfixParser;

    /**
     * Constructs a CalculatorEngine with default division precision and default trigonometric mode (DEG).
     */
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
        this.postfixParser = new PostfixParser();
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
        if (expression.isBlank()) {
            return BigNumbers.ZERO;
        }

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
        List<Token> postfix = postfixParser.toPostfix(tokens);

        // Evaluate the postfix expression to a BigDecimal result
        return evaluator.evaluate(postfix).trim();
    }

}
