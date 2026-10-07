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

package io.github.lembergmax.justmath.benchmarks;

import java.util.concurrent.TimeUnit;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.calculator.CalculatorEngine;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Measures the throughput of the expression engine for a short and for a longer expression, with the expression cache
 * on and off. A cache hit skips the tokenizer and the parser, so the difference shows what parsing costs.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class EngineBenchmark {

    private static final String SHORT_EXPRESSION = "2+3*4-5/2";

    private static final String LONG_EXPRESSION = "sqrt(16)*2^10+sin(30)*cos(60)+10!/8!-summation(1;20;k^2)";

    @Param({"short", "long"})
    public String expressionKind;

    private CalculatorEngine cachedEngine;

    private CalculatorEngine uncachedEngine;

    private String expression;

    @Setup
    public void setup() {
        expression = "short".equals(expressionKind) ? SHORT_EXPRESSION : LONG_EXPRESSION;

        cachedEngine = new CalculatorEngine();
        cachedEngine.setExpressionCacheEnabled(true);

        uncachedEngine = new CalculatorEngine();
        uncachedEngine.setExpressionCacheEnabled(false);
    }

    @Benchmark
    public BigNumber evaluateWithCache() {
        return cachedEngine.evaluate(expression);
    }

    @Benchmark
    public BigNumber evaluateWithoutCache() {
        return uncachedEngine.evaluate(expression);
    }
}
