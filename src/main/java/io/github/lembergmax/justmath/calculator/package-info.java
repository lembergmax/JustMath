/*
 * Copyright (c) 2026 Max Lemberg
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
/**
 * The expression engine.
 *
 * <p>{@link io.github.lembergmax.justmath.calculator.CalculatorEngine} evaluates mathematical expressions
 * given as strings. It offers four families of {@code evaluate} methods that differ in return type and in
 * how failures are reported. Internally an expression passes through three stages: the tokenizer splits it
 * into tokens, the postfix parser orders them with the shunting-yard algorithm, and the evaluator runs the
 * result on a value stack.</p>
 *
 * <p>Input is parsed with {@code .} as the decimal separator and {@code ;} as the argument separator
 * unless an input locale is set. The engine locale only controls how results are formatted and in which
 * language errors are reported. An engine is meant to be used from one thread at a time.</p>
 */
package io.github.lembergmax.justmath.calculator;
