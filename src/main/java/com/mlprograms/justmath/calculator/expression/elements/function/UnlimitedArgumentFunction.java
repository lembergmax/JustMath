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

package com.mlprograms.justmath.calculator.expression.elements.function;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import com.mlprograms.justmath.calculator.exceptions.ProcessingErrorException;
import com.mlprograms.justmath.calculator.expression.operations.function.UnlimitedArgumentFunctionOperation;

import java.math.MathContext;
import java.util.*;

public class UnlimitedArgumentFunction extends Function {

    private final UnlimitedArgumentFunctionOperation operation;

    public UnlimitedArgumentFunction(String symbol, int precedence, UnlimitedArgumentFunctionOperation operation) {
        super(symbol, precedence);
        this.operation = operation;
    }

    @Override
    public void apply(Deque<Object> stack, MathContext mathContext, TrigonometricMode trigonometricMode, Locale locale) {
        if (stack.isEmpty()) {
            throw new ProcessingErrorException("Function '" + getSymbol() + "' requires an argument count on the stack");
        }

        Object countObject = stack.pop();
        if (!(countObject instanceof BigNumber countNumber)) {
            throw new ProcessingErrorException("Invalid argument count for function '" + getSymbol() + "': " + countObject);
        }

        int argumentCount = countNumber.intValue();
        if (argumentCount <= 0) {
            throw new ProcessingErrorException("Function '" + getSymbol() + "' requires at least one argument, but got " + argumentCount);
        }

        if (stack.size() < argumentCount) {
            throw new ProcessingErrorException("Function '" + getSymbol() + "' expected " + argumentCount + " arguments but stack contains only " + stack.size());
        }

        List<BigNumber> arguments = new ArrayList<>(argumentCount);
        for (int i = 0; i < argumentCount; i++) {
            Object value = stack.pop();
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
