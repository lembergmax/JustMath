/*
 * Copyright (c) 2025-2026 Max Lemberg
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
import com.mlprograms.justmath.bignumber.BigNumberCoordinate;
import com.mlprograms.justmath.calculator.errors.CalculatorErrorCode;
import com.mlprograms.justmath.calculator.exceptions.ProcessingErrorException;
import com.mlprograms.justmath.calculator.exceptions.SyntaxErrorException;
import com.mlprograms.justmath.calculator.expression.ExpressionElement;
import com.mlprograms.justmath.calculator.internal.Token;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import lombok.NonNull;

import java.math.MathContext;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;

import static com.mlprograms.justmath.bignumber.BigNumbers.CALCULATION_LOCALE;

/**
 * Evaluates a mathematical expression represented as a list of tokens in Reverse Polish Notation.
 * Supports full precision using BigDecimal.
 */
class Evaluator {

    /**
     * Math context specifying the precision and rounding mode for calculations.
     */
    private final MathContext mathContext;

    /**
     * The mode used for trigonometric calculations (e.g., degrees or radians).
     */
    private final TrigonometricMode trigonometricMode;

    /**
     * Creates an evaluator with the supplied configuration. Both arguments are mandatory because
     * every {@code apply()} call dispatched from {@link #evaluate(List)} forwards them to the
     * concrete {@link ExpressionElement} implementations — leaving either as {@code null} would
     * produce a {@link NullPointerException} on the first operator. The previously generated
     * Lombok {@code @NoArgsConstructor} silently created an evaluator in that broken state.
     *
     * @param mathContext       math context controlling precision and rounding; must not be {@code null}
     * @param trigonometricMode trigonometric mode; must not be {@code null}
     */
    Evaluator(@NonNull final MathContext mathContext, @NonNull final TrigonometricMode trigonometricMode) {
        this.mathContext = mathContext;
        this.trigonometricMode = trigonometricMode;
    }

    /**
     * Evaluates a list of tokens in Reverse Polish Notation (RPN) and returns the final result
     * as a {@link BigNumber}.
     *
     * <p>The method drives a classic stack-based evaluator: numeric and string literals are
     * pushed verbatim; operators, unary operators, functions, and constants are resolved through
     * the {@link ExpressionElement} registry and consume / push values on the stack. Intermediate
     * values may be either {@link BigNumber} scalars or domain types such as {@link BigNumberCoordinate}
     * (the {@code Pol}/{@code Rec} two-component results). Scalar operators that need to consume a
     * coordinate go through {@link com.mlprograms.justmath.bignumber.math.utils.MathUtils#ensureScalar
     * ensureScalar}, which projects the coordinate onto its first component.</p>
     *
     * <p><strong>Final result contract:</strong> the top of the stack must be a {@link BigNumber}.
     * If a multi-component value such as a {@link BigNumberCoordinate} ends up as the lone stack
     * entry, this method throws a {@link ProcessingErrorException} — the engine does not silently
     * collapse it. Callers that want to read both components should keep using the typed
     * {@code Pol}/{@code Rec} helpers exposed by {@link com.mlprograms.justmath.bignumber.BigNumber}.</p>
     *
     * @param reversePolishNotationTokens a list of {@link Token} objects in Reverse Polish Notation
     * @return the result of evaluating the expression as a {@link BigNumber}
     * @throws SyntaxErrorException     if a token is malformed, an operator lacks an operand, or
     *                                  the final stack does not contain exactly one value
     * @throws ProcessingErrorException if the final value cannot be represented as a {@link BigNumber}
     */
    public BigNumber evaluate(List<Token> reversePolishNotationTokens) {
        Deque<Object> stack = new ArrayDeque<>();

        for (Token token : reversePolishNotationTokens) {
            switch (token.getType()) {
                case NUMBER -> stack.push(new BigNumber(token.getValue()));
                case STRING -> stack.push(token.getValue());
                case OPERATOR, UNARY_OPERATOR, FUNCTION, CONSTANT -> {
                    ExpressionElement expressionElement = token.asArithmeticOperator()
                            .orElseThrow(() -> new SyntaxErrorException(
                                    CalculatorErrorCode.SYNTAX_UNKNOWN_FUNCTION,
                                    java.util.Map.of("function", token.getValue()),
                                    "Unknown operator or function: " + token.getValue(),
                                    null));

                    try {
                        expressionElement.apply(stack, mathContext, trigonometricMode, CALCULATION_LOCALE);
                    } catch (NoSuchElementException stackUnderflow) {
                        throw new SyntaxErrorException(
                                CalculatorErrorCode.SYNTAX_MISSING_OPERAND,
                                java.util.Map.of("operator", token.getValue()),
                                "Incomplete expression: operator or function '" + token.getValue() + "' is missing an operand",
                                null);
                    }
                }
                default -> throw new ProcessingErrorException(CalculatorErrorCode.PROCESSING_INTERNAL, "Unexpected token: " + token);
            }
        }

        if (stack.size() != 1) {
            throw new SyntaxErrorException(CalculatorErrorCode.SYNTAX_UNEXPECTED_END, "Incomplete expression: expected a single result, but found " + stack.size());
        }

        Object result = stack.pop();
        BigNumber finalResult;

        if (result instanceof BigNumber bigNumber) {
            finalResult = bigNumber;
        } else {
            throw new ProcessingErrorException(CalculatorErrorCode.PROCESSING_INTERNAL, "Unsupported result type: " + result);
        }

        return finalResult;
    }

}
