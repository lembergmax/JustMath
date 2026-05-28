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

package com.mlprograms.justmath.calculator.expression.elements.function;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.MultiValueResult;
import com.mlprograms.justmath.calculator.errors.CalculatorErrorCode;
import com.mlprograms.justmath.calculator.exceptions.ProcessingErrorException;
import com.mlprograms.justmath.calculator.exceptions.SyntaxErrorException;
import com.mlprograms.justmath.calculator.expression.operations.function.UnlimitedArgumentFunctionOperation;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;

import java.math.MathContext;
import java.util.*;

/**
 * Variadic function element — operates on an arbitrary, runtime-determined number of arguments
 * (e.g. {@code sum}, {@code avg}, {@code median}).
 *
 * <p><strong>Scalar coercion of multi-value results:</strong> when an argument popped from the
 * evaluation stack is a {@link MultiValueResult} (for example a {@code Pol(...)}/{@code Rec(...)}
 * coordinate pair), only its {@link MultiValueResult#firstValue() first component} is forwarded
 * to the variadic operation. The second component is silently discarded. This mirrors the
 * project-wide scalarisation policy in {@link com.mlprograms.justmath.bignumber.math.utils.MathUtils#ensureScalar},
 * which lets expressions such as {@code sum(Pol(3;4); 5)} type-check at evaluation time. Callers
 * who need both components must consume them via dedicated coordinate-aware functions, not
 * variadics.</p>
 */
public class UnlimitedArgumentFunction extends Function {

    private final UnlimitedArgumentFunctionOperation operation;

    public UnlimitedArgumentFunction(String symbol, int precedence, UnlimitedArgumentFunctionOperation operation) {
        super(symbol, precedence);
        this.operation = operation;
    }

    @Override
    public void apply(Deque<Object> stack, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
        if (stack.isEmpty()) {
            throw new SyntaxErrorException(
                    CalculatorErrorCode.ARGUMENT_COUNT_MISMATCH,
                    Map.of("function", getSymbol(), "expected", "1+", "actual", "0"),
                    "Function '" + getSymbol() + "' requires an argument count on the stack",
                    null);
        }

        Object countObject = stack.pop();
        if (!(countObject instanceof BigNumber countNumber)) {
            throw new SyntaxErrorException(
                    CalculatorErrorCode.ARGUMENT_COUNT_MISMATCH,
                    Map.of("function", getSymbol(), "expected", "1+", "actual", "0"),
                    "Invalid argument count for function '" + getSymbol() + "': " + countObject,
                    null);
        }

        int argumentCount = countNumber.intValue();
        if (argumentCount <= 0) {
            throw new SyntaxErrorException(
                    CalculatorErrorCode.ARGUMENT_COUNT_MISMATCH,
                    Map.of("function", getSymbol(), "expected", "1+", "actual", String.valueOf(argumentCount)),
                    "Function '" + getSymbol() + "' requires at least one argument, but got " + argumentCount,
                    null);
        }

        if (stack.size() < argumentCount) {
            throw new SyntaxErrorException(
                    CalculatorErrorCode.ARGUMENT_COUNT_MISMATCH,
                    Map.of("function", getSymbol(), "expected", String.valueOf(argumentCount), "actual", String.valueOf(stack.size())),
                    "Function '" + getSymbol() + "' expected " + argumentCount + " arguments but stack contains only " + stack.size(),
                    null);
        }

        List<BigNumber> arguments = new ArrayList<>(argumentCount);
        for (int i = 0; i < argumentCount; i++) {
            Object value = stack.pop();
            if (value instanceof MultiValueResult multiValueResult) {
                value = multiValueResult.firstValue();
            }
            if (!(value instanceof BigNumber bigNumber)) {
                throw new ProcessingErrorException("Invalid argument type for function '" + getSymbol() + "': " + value);
            }

            arguments.add(bigNumber);
        }

        Collections.reverse(arguments);

        BigNumber first = arguments.getFirst();
        List<BigNumber> rest = arguments.subList(1, arguments.size());

        BigNumber result = operation.apply(first, rest, mathContext, locale);
        stack.push(result);
    }

}
